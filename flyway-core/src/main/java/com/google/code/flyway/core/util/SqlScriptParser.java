/**
 * Copyright (C) 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.flyway.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a textual SQL script containing a group of database commands separated by semi-colons and converts it into an
 * array of {@link String}s, suitable for processing through JDBC or any other desired mechanism.
 * <p/>
 * This code is based loosely on source code from the <i>Apache Ant</i> project at:
 * <p/>
 * <code>http://ant.apache.org/index.html</code>
 * <p/>
 * As mentioned, multiple statements can be provided, separated by semi-colons. Lines within the script can be commented
 * using either "--" or "//". Comments extend to the end of the line.
 */
public class SqlScriptParser {

    /**
     * The logger class.
     */
    private static final Log s_log = LogFactory.getLog(SqlScriptParser.class);

    /**
     * The default Statement delimiter.
     */
    private static final String DEFAULT_STATEMENT_DELIMITER = ";";

    /**
     * Parse the SQL script to produce an array of database statements.
     *
     * @param sqlScriptReader A reader that reads the SQL script text.
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql migration scripts.
     * @return An array of strings, each containing a single statement.
     */
    public static String[] parse(Reader sqlScriptReader, Map<String, String> placeholders) {
        Collection<String> statements = new ArrayList<String>();

        StringBuilder sql = new StringBuilder();
        String line = "";
        BufferedReader in = new BufferedReader(sqlScriptReader);
        int lineNumber = 0;
        String statementDelimiter = DEFAULT_STATEMENT_DELIMITER;

        try {
            // Read each line and build up statements.
            while ((line = in.readLine()) != null) {
                lineNumber++;
                String noPlaceholdersLine = replacePlaceholders(line, placeholders);
                parseLine(noPlaceholdersLine, sql, lineNumber); // Trim, validate, and strip comments.
                if (sql.length() > 0) {
                    if (sql.toString().toUpperCase().endsWith("BEGIN")) {
                        statementDelimiter = "/";
                    }
                    if (statementDelimiter.equals(sql.substring(sql.length() - 1))) {
                        // This line terminates the statement.
                        String statement = sql.toString().substring(0, sql.length() - 1).trim(); // Lose
                        // the
                        // delimiter;
                        if (statement.length() > 0) {
                            statements.add(statement);
                            s_log.debug("Found statement: " + statement);
                        }
                        sql.replace(0, sql.length(), ""); // Clear buffer for the next statement.
                        statementDelimiter = DEFAULT_STATEMENT_DELIMITER;
                    } else {
                        // This line does not terminate the statement. Add a space and go on to the
                        // next one.
                        sql.append(" ");
                    }
                }
            }
        } catch (IOException e) {
            // Convert to unchecked parse exception.
            throw new IllegalStateException("Cannot read script", e);
        }

        // Catch any statements not followed by delimiter.
        String orphanStatement = sql.toString().trim();
        if (orphanStatement.length() > 0) {
            statements.add(orphanStatement);
            s_log.debug("Found statement: " + orphanStatement);
        }

        String[] result = new String[statements.size()];
        statements.toArray(result);
        return result;
    }

    /**
     * Replaces the placeholders in this text with their values.
     *
     * @param text The text to analyse.
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql migration scripts.
     * @return The text with placeholders replaced.
     */
    private static String replacePlaceholders(String text, Map<String, String> placeholders) {
        String result = text;
        for (String placeholder: placeholders.keySet()) {
            String searchTerm = "${" + placeholder + "}";
            result = result.replaceAll(Pattern.quote(searchTerm),
                    Matcher.quoteReplacement(placeholders.get(placeholder)));
        }
        return result;
    }

    /**
     * Parse a line of text to remove comments.
     *
     * @param line       The line to parse. The string must not have any new line, carriage return, or form feed
     *                   characters. It also may not contain a double-quote outside of a string literal, and the
     *                   statement delimiter character may only appear at the end of the line.
     * @param sql        String buffer that stores the result, which is appended to the end. When the method returns, it
     *                   will contain the trimmed line with comments removed. If the line contains no usable SQL
     *                   fragment, nothing is appended.
     * @param lineNumber Line number used for exceptions.
     * @throws IllegalArgumentException if the line is null or contains one of the line terminating characters mentioned
     *                                  above.
     */
    private static void parseLine(String line, StringBuilder sql, int lineNumber) {

        if (line == null) {
            throw new IllegalArgumentException("Object cannot be null.");
        }
        // Must be a single line of text.
        if (line.indexOf("\n") >= 0 || line.indexOf("\r") >= 0 || line.indexOf("\f") >= 0) {
            // This is a programmer error, not a parse error, so throw IllegalArgumentException.
            throw new IllegalArgumentException(
                    "Line string may not embed new lines, form feeds, or carriage returns.");
        }

        String trimmedLine = line.trim();

        // Parse line looking for single quote string delimiters. Anything that's part of a string
        // just passes through.
        StringTokenizer quoteTokenizer = new StringTokenizer(trimmedLine, "'", true);
        boolean inLiteral = false; // true if we're parsing through a string literal
        while (quoteTokenizer.hasMoreTokens()) {
            String token = quoteTokenizer.nextToken();
            if (token.equals("'")) {
                // Token is a string delimiter. Toggle "inLiteral" flag.
                inLiteral = !inLiteral;
            } else if (!inLiteral) {
                // Look for EOL comments.
                int commentIndex = indexOfComment(token);
                if (commentIndex >= 0) {
                    // Truncate token to the comment marker.
                    token = token.substring(0, commentIndex).trim();
                }

                // Thwart any attempt to use double-quote outside of a string literal.
                if (token.indexOf("\"") >= 0) {
                    throw new IllegalStateException("Double Quotation Not Allowed. Line: " + lineNumber);
                }
                // Thwart any attempt to have the statement delimiter embedded in a line. Not
                // supported at this point.
                int statementEndIndex = token.indexOf(DEFAULT_STATEMENT_DELIMITER);
                if (statementEndIndex >= 0 && statementEndIndex != (token.length() - 1)) {
                    throw new IllegalStateException("Multiple Statements Per Line Not Allowed. Line: " + lineNumber);
                }

                // If we've hit a comment, we're done with this line. Just append the token and
                // break out.
                if (commentIndex >= 0) {
                    sql.append(token);
                    break;
                }

            } // Else, we're in a string literal. Just let it pass through.
            sql.append(token);
        } // end while

        if (inLiteral) {
            // If the inLiteral flag is set here, the line has an unterminated string literal.
            throw new IllegalStateException("Unterminated String Literal. Line: " + lineNumber);
        }
    }

    /**
     * Return the index of the fist comment marker in the string. Comment markers are "--" and "//".
     *
     * @param s The string to search.
     * @return The index of the first occurrence of a comment marker or -1 if no marker is found.
     */
    private static int indexOfComment(String s) {
        int comment1Index = s.indexOf("//");
        int comment2Index = s.indexOf("--");
        int result;
        if (comment1Index >= 0) {
            if (comment2Index >= 0) {
                result = Math.min(comment2Index, comment1Index);
            } else {
                result = comment1Index;
            }
        } else {
            result = comment2Index;
        }
        return result;
    }
}
