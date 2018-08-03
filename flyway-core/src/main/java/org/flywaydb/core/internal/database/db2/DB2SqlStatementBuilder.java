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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting DB2-specific delimiter changes.
 */
public class DB2SqlStatementBuilder extends SqlStatementBuilder {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "--#SET TERMINATOR";

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(DO)(?!.)");

    /**
     * Regex for statements that accept blocks.
     */
    private static final Pattern BLOCK_STATEMENT_REGEX =
            Pattern.compile("^CREATE( OR REPLACE)? (FUNCTION|PROCEDURE|TRIGGER)(\\s.*)?");

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    /**
     * The current delimiter to use. This delimiter can be changed
     * as well as temporarily disabled inside BEGIN END; blocks.
     */
    private Delimiter currentDelimiter = defaultDelimiter;

    private String previousLine = "";

    /**
     * Creates a new SqlStatementBuilder.
     */
    public DB2SqlStatementBuilder() {
        super(Delimiter.SEMICOLON);
    }

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
            // due to a --#SET TERMINATOR directive earlier in the SQL script
            currentDelimiter = delimiter;
        }

        if (hasNonCommentPart() && StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        return currentDelimiter;
    }

    @Override
    protected boolean isBlockStatement() {
        return BLOCK_STATEMENT_REGEX.matcher(statementStart).matches();
    }

    @Override
    protected boolean isBlockBeginToken(String token) {
        return "BEGIN".equals(token)
                || "CASE".equals(token)
                || "IF".equals(token)
                || "DO".equals(token) // Used by FOR and WHILE loops
                || "LOOP".equals(token)
                || "REPEAT".equals(token);
    }

    @Override
    protected boolean isBlockEndToken(String token) {
        return "END".equals(token);
    }

    @Override
    protected Collection<String> tokenizeLine(String line) {
        String processedLine = line;
        if (previousLine.endsWith("END")) {
            if (line.startsWith("IF")) {
                processedLine = processedLine.substring(2);
            } else if (line.startsWith("FOR")) {
                processedLine = processedLine.substring(3);
            } else if (line.startsWith("CASE")) {
                processedLine = processedLine.substring(4);
            } else if (line.startsWith("LOOP")) {
                processedLine = processedLine.substring(4);
            } else if (line.startsWith("WHILE")) {
                processedLine = processedLine.substring(5);
            } else if (line.startsWith("REPEAT")) {
                processedLine = processedLine.substring(6);
            }
        }

        if (StringUtils.hasLength(line)) {
            previousLine = line;
        }

        return super.tokenizeLine(
                processedLine.replaceAll("END (IF|FOR|CASE|LOOP|WHILE|REPEAT)", "END"));
    }
}