/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a SQL statement, one line at a time.
 */
public class SqlStatementBuilder {
    /**
     * The current statement, as it is being built.
     */
    private StringBuilder statement = new StringBuilder();

    /**
     * The initial line number of this statement.
     */
    private int lineNumber;

    /**
     * Flag indicating whether the current statement is still empty.
     */
    private boolean empty = true;

    /**
     * Flag indicating whether the current statement is properly terminated.
     */
    private boolean terminated;

    /**
     * Are we currently inside a ' multi-line string literal.
     */
    private boolean insideQuoteStringLiteral = false;

    /**
     * Are we currently inside an alternate multi-line string literal.
     */
    private boolean insideAlternateQuoteStringLiteral = false;

    /**
     * The alternate quote that is expected to close the string literal.
     */
    private String alternateQuote;

    /**
     * Are we inside a multi-line /*  *&#47; comment.
     */
    private boolean insideMultiLineComment = false;

    /**
     * The current delimiter to look for to terminate the statement.
     */
    private Delimiter delimiter = getDefaultDelimiter();

    /**
     * If true, JDBC escape processing will be enabled. If false, JDBC escape processing will be disabled. If null, JDBC escape processing will be left at the default value.
     */
    private Boolean useSqlEscape = null;

    /**
     * @return true if JDBC escape processing will be enabled. false if JDBC escape processing will be disabled. null if JDBC escape processing will be left at the default value
     */
    public Boolean isUseSqlEscape() {
        return useSqlEscape;
    }

    /**
     * @param useSqlEscape If true, JDBC escape processing will be enabled. If false, JDBC escape processing will be disabled. If null, JDBC escape processing will be left at the default value
     */
    protected void setUseSqlEscape(Boolean useSqlEscape) {
        this.useSqlEscape = useSqlEscape;
    }

    /**
     * @return The default delimiter for this database.
     */
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter(";", false);
    }

    /**
     * @param lineNumber The initial line number of this statement.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * @param delimiter The current delimiter to look for to terminate the statement.
     */
    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Checks whether the statement is still empty.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Checks whether the statement being built is now properly terminated.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @return The assembled statement, with the delimiter stripped off.
     */
    public SqlStatement getSqlStatement() {
        return new SqlStatement(lineNumber, statement.toString(), isUseSqlEscape());
    }

    /**
     * Analyses this line ánd extracts the new default delimiter.
     * This method is only called between statements and looks for explicit delimiter change directives.
     *
     * @return The new delimiter. {@code null} if it is the same as the current one.
     */
    @SuppressWarnings("UnusedParameters")
    public Delimiter extractNewDelimiterFromLine(String line) {
        return null;
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is a directive that should be processed by the database, {@code false} if not.
     */
    public boolean isCommentDirective(String line) {
        return false;
    }

    /**
     * Checks whether this line is just a single-line comment outside a statement or not.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is, {@code false} if not.
     */
    public boolean isSingleLineComment(String line) {
        return line.startsWith("--");
    }

    /**
     * Adds this line to the current statement being built.
     *
     * @param line The line to add.
     */
    public void addLine(String line) {
        if (isEmpty()) {
            empty = false;
        } else {
            statement.append("\n");
        }

        String lineSimplified = simplifyLine(line);

        if (endsWithOpenMultilineStringLiteral(lineSimplified)) {
            statement.append(line);
            return;
        }

        delimiter = changeDelimiterIfNecessary(lineSimplified, delimiter);

        statement.append(line);

        if (lineTerminatesStatement(lineSimplified, delimiter)) {
            //TODO: Check if the delimiter can be stripped from the existing statement instead
            statement = new StringBuilder(stripDelimiter(statement.toString(), delimiter));
            terminated = true;
        }
    }

    /**
     * Simplifies this line to make it easier to parse.
     *
     * @param line The line to simplify.
     * @return The simplified line.
     */
    protected String simplifyLine(String line) {
        return line.replaceAll("\\s+", " ").trim().toUpperCase();
    }

    /**
     * Checks whether this line in the sql script indicates that the statement delimiter will be different from the
     * current one. Useful for database-specific stored procedures and block constructs.
     *
     * @param line      The line to analyse.
     * @param delimiter The current delimiter.
     * @return The new delimiter to use (can be the same as the current one) or {@code null} for no delimiter.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        return delimiter;
    }

    /**
     * Checks whether this line terminates the current statement.
     *
     * @param line      The line to check.
     * @param delimiter The current delimiter.
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    private boolean lineTerminatesStatement(String line, Delimiter delimiter) {
        if (delimiter == null) {
            return false;
        }

        String upperCaseDelimiter = delimiter.getDelimiter().toUpperCase();

        if (delimiter.isAloneOnLine()) {
            return line.equals(upperCaseDelimiter);
        }

        return line.endsWith(upperCaseDelimiter);
    }

    /**
     * Strips this delimiter from this sql statement.
     *
     * @param sql       The statement to parse.
     * @param delimiter The delimiter to strip.
     * @return The sql statement without delimiter.
     */
    /* private -> testing */
    static String stripDelimiter(String sql, Delimiter delimiter) {
        final int lowerCaseIndex = sql.lastIndexOf(delimiter.getDelimiter().toLowerCase());
        final int upperCaseIndex = sql.lastIndexOf(delimiter.getDelimiter().toUpperCase());
        return sql.substring(0, Math.max(lowerCaseIndex, upperCaseIndex));
    }

    /**
     * Extracts the alternate open quote from this token (if any).
     *
     * @param token The token to check.
     * @return The alternate open quote. {@code null} if none.
     */
    protected String extractAlternateOpenQuote(String token) {
        return null;
    }

    /**
     * Computes the alternate closing quote for this open quote.
     *
     * @param openQuote The alternate open quote.
     * @return The close quote.
     */
    protected String computeAlternateCloseQuote(String openQuote) {
        return openQuote;
    }

    /**
     * Checks whether this line ends the statement with an open multi-line string literal (which will be
     * continued on the next line).
     *
     * @param line The line that was just added to the statement.
     * @return {@code true} if the statement is unfinished and the end is currently in the middle of a multi-line string
     *         literal. {@code false} if not.
     */
    protected boolean endsWithOpenMultilineStringLiteral(String line) {
        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(line, " @<>;:=|(),+{}");

        List<TokenType> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        for (TokenType delimitingToken : delimitingTokens) {
            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral
                    && TokenType.MULTI_LINE_COMMENT.equals(delimitingToken)) {
                insideMultiLineComment = !insideMultiLineComment;
            }

            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral && !insideMultiLineComment
                    && TokenType.SINGLE_LINE_COMMENT.equals(delimitingToken)) {
                return false;
            }

            if (!insideMultiLineComment && !insideQuoteStringLiteral &&
                    TokenType.ALTERNATE_QUOTE.equals(delimitingToken)) {
                insideAlternateQuoteStringLiteral = !insideAlternateQuoteStringLiteral;
            }

            if (!insideMultiLineComment && !insideAlternateQuoteStringLiteral &&
                    TokenType.QUOTE.equals(delimitingToken)) {
                insideQuoteStringLiteral = !insideQuoteStringLiteral;
            }
        }

        return insideQuoteStringLiteral || insideAlternateQuoteStringLiteral;
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     *         impact on string delimiting are discarded.
     */
    private List<TokenType> extractStringLiteralDelimitingTokens(String[] tokens) {
        List<TokenType> delimitingTokens = new ArrayList<TokenType>();
        for (String token : tokens) {
            String cleanToken = removeCharsetCasting(removeEscapedQuotes(token));

            if (alternateQuote == null) {
                String alternateQuoteFromToken = extractAlternateOpenQuote(cleanToken);
                if (alternateQuoteFromToken != null) {
                    String closeQuote = computeAlternateCloseQuote(alternateQuoteFromToken);
                    if (cleanToken.length() >= (alternateQuoteFromToken.length() + closeQuote.length())
                            && cleanToken.startsWith(alternateQuoteFromToken) && cleanToken.endsWith(closeQuote)) {
                        //Skip $$abc$$, ...
                        continue;
                    }

                    alternateQuote = closeQuote;
                    delimitingTokens.add(TokenType.ALTERNATE_QUOTE);

                    if (cleanToken.startsWith("\"") && cleanToken.endsWith("'")) {
                        // add QUOTE token for cases where token starts with a " and ends with a '
                        delimitingTokens.add(TokenType.QUOTE);
                    }

                    continue;
                }
            }
            if ((alternateQuote != null) && cleanToken.endsWith(alternateQuote)) {
                alternateQuote = null;
                delimitingTokens.add(TokenType.ALTERNATE_QUOTE);
                continue;
            }

            if ((cleanToken.length() >= 2) && cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                //Skip '', 'abc', ...
                continue;
            }
            if (cleanToken.startsWith("'") || cleanToken.endsWith("'")) {
                delimitingTokens.add(TokenType.QUOTE);
            }

            if (isSingleLineComment(cleanToken)) {
                delimitingTokens.add(TokenType.SINGLE_LINE_COMMENT);
            }

            if ((cleanToken.length() >= 4) && cleanToken.startsWith("/*") && cleanToken.endsWith("*/")) {
                //Skip /**/, /*comment*/, ...
                continue;
            }
            if (cleanToken.startsWith("/*") || cleanToken.endsWith("*/")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT);
            }
        }

        return delimitingTokens;
    }

    /**
     * Removes escaped quotes from this token.
     * @param token The token to parse.
     * @return The cleaned token.
     */
    protected String removeEscapedQuotes(String token) {
        return StringUtils.replaceAll(token, "''", "");
    }

    /**
     * Removes charset casting that prefixes string literals.
     * Must be implemented in dialect specific sub classes.
     * @param token The token to parse.
     * @return The cleaned token.
     */
    protected String removeCharsetCasting(String token) {
        return token;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private static enum TokenType {
        /**
         * Token opens or closes a ' string literal
         */
        QUOTE,

        /**
         * Token opens or closes an alternate string literal
         */
        ALTERNATE_QUOTE,

        /**
         * Token starts end of line comment
         */
        SINGLE_LINE_COMMENT,

        /**
         * Token opens or closes multi-line comment
         */
        MULTI_LINE_COMMENT
    }
}
