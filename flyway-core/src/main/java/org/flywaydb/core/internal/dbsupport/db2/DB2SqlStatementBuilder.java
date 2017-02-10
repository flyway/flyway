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

/**
 * SqlStatementBuilder supporting DB2-specific delimiter changes.
 */
public class DB2SqlStatementBuilder extends SqlStatementBuilder {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "--#SET TERMINATOR";

    /**
     * Are we currently inside a BEGIN END; block?
     */
    private boolean insideBeginEndBlock;

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
            currentDelimiter = new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
            return currentDelimiter;
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
        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (statementStart.startsWith("CREATE FUNCTION")
                || statementStart.startsWith("CREATE PROCEDURE")
                || statementStart.startsWith("CREATE TRIGGER")
                || statementStart.startsWith("CREATE OR REPLACE FUNCTION")
                || statementStart.startsWith("CREATE OR REPLACE PROCEDURE")
                || statementStart.startsWith("CREATE OR REPLACE TRIGGER")) {
            if (line.startsWith("BEGIN")) {
                insideBeginEndBlock = true;
            }

            if (line.endsWith("END;")) {
                insideBeginEndBlock = false;
            }
        }

        if (insideBeginEndBlock) {
            return null;
        }
        return currentDelimiter;
    }
}
