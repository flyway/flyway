/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FirebirdSqlStatementBuilder borrowed from DB2, use --set term before stored procs or triggers
 */
public class FirebirdSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Holds the beginning of the statement.
     */

    private String statementStart = "";

    FirebirdSqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "--SET TERM";

    /**
     * Regex to check for a BEGIN statement of a SQL PL block (Optional label followed by BEGIN).
     */
    private static final Pattern BEGIN_REGEX = Pattern.compile("((([A-Z]+[A-Z0-9]*)\\s?:\\s?)|(.*\\s))?BEGIN(\\sATOMIC)?(\\s.*)?");

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(DO)(?!.)");

    /**
     * The labels associated with nested BEGIN ... END blocks.
     */
    private Deque<String> beginEndLabels = new LinkedList<>();


    /**
     * The current delimiter to use. This delimiter can be changed
     * as well as temporarily disabled inside BEGIN END; blocks.
     */
    private Delimiter currentDelimiter = defaultDelimiter;

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return null;
    }

    @Override
    protected boolean isSingleLineComment(String line) {
        return line.startsWith("--") && !line.startsWith(DELIMITER_KEYWORD);
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("X'")) {
            return token.substring(token.indexOf("'"));
        }

        Matcher afterMatcher = KEYWORDS_AFTER_STRING_LITERAL_REGEX.matcher(token);
        if (afterMatcher.find()) {
            token = afterMatcher.group(1);
        }

        return super.cleanToken(token);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (delimiter != null && !delimiter.equals(currentDelimiter)) {
            // Synchronize current delimiter with main delimiter in case it was changed
            // due to a --SET TERM directive earlier in the SQL script
            currentDelimiter = delimiter;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (!";".equals(currentDelimiter.getDelimiter())) {
            return currentDelimiter;
        }

        if (statementStart.matches("^CREATE( OR REPLACE)? (FUNCTION|PROCEDURE|TRIGGER)(\\s.*)?")) {
            if (isBegin(line) || line.matches("(.*\\s)?CASE(\\sWHEN)?(\\s.*)?")) {
                beginEndLabels.addLast(extractLabel(line));
            }

            if (isEnd(line, beginEndLabels.isEmpty() ? null : beginEndLabels.getLast(), currentDelimiter, beginEndLabels.size())) {
                beginEndLabels.removeLast();
            }
        }

        if (!beginEndLabels.isEmpty()) {
            return null;
        }
        return currentDelimiter;
    }

    static boolean isBegin(String line) {
        return BEGIN_REGEX.matcher(line).find();
    }

    static String extractLabel(String line) {
        Matcher matcher = BEGIN_REGEX.matcher(line);
        return line.contains(":") && matcher.matches() ? matcher.group(3) : null;
    }

    static boolean isEnd(String line, String label, Delimiter currentDelimiter, int beginEndDepth) {
        String actualDelimiter = beginEndDepth > 1 ? ";" : currentDelimiter.getDelimiter();

        return line.matches(
                // First optionally match preceding part of statement
                "(.*\\s)?"
                        // Then require END
                        + "END"
                        // Now optionally match label
                        + (label == null ? "" : "(\\s" + Pattern.quote(label) + ")?")
                        // Finally optionally match delimiter
                        + "\\s?(" + Pattern.quote(actualDelimiter) + ")?");
    }



}