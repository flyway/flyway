/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

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
     * Regex to check for a BEGIN statement of a SQL PL block (Optional label followed by BEGIN).
     */
    private static final Pattern BEGIN_REGEX = Pattern.compile("^(([A-Z]+[A-Z0-9]*)\\s?:\\s?)?BEGIN(\\sATOMIC)?(\\s.*)?");

    /**
     * How deep are we inside a BEGIN ... END blocks?
     */
    private int beginEndDepth;

    /**
     * The label for the current BEGIN ... END block.
     */
    private String label;

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    /**
     * The current delimiter to use. This delimiter can be changed
     * as well as temporarily disabled inside BEGIN END; blocks.
     */
    private Delimiter currentDelimiter = getDefaultDelimiter();

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
        return super.cleanToken(token);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (delimiter != null && !delimiter.equals(currentDelimiter)) {
            // Synchronize current delimiter with main delimiter in case it was changed
            // due to a --#SET TERMINATOR directive earlier in the SQL script
            currentDelimiter = delimiter;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (statementStart.matches("^CREATE( OR REPLACE)? (FUNCTION|PROCEDURE|TRIGGER)(\\s.*)?")) {
            if (isBegin(line)) {
                if (beginEndDepth == 0) {
                    label = extractLabel(line);
                }
                beginEndDepth++;
            }

            if (isEnd(line)) {
                beginEndDepth--;
            }
        }

        if (beginEndDepth > 0) {
            return null;
        }
        return currentDelimiter;
    }

    static boolean isBegin(String line) {
        return BEGIN_REGEX.matcher(line).find();
    }

    static String extractLabel(String line) {
        Matcher matcher = BEGIN_REGEX.matcher(line);
        return line.contains(":") && matcher.matches() ? matcher.group(2) : null;
    }

    private boolean isEnd(String line) {
        if (label == null) {
            return line.matches(".*\\s?END\\s?(" + Pattern.quote(currentDelimiter.getDelimiter()) + ")?");
        }
        return line.matches(".*\\s?END(\\s" + Pattern.quote(label) + ")?\\s?(" + Pattern.quote(currentDelimiter.getDelimiter()) + ")?");
    }
}