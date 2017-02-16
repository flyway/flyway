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
package org.flywaydb.core.internal.dbsupport.saphana;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * SqlStatementBuilder supporting SAP HANA-specific delimiter changes.
 */
public class SapHanaSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Are we currently inside a BEGIN END; block? How deeply are begin/end nested?
     */
    private int beginEndNestedDepth = 0;
    private boolean insideStatementAllowingNestedBeginEndBlocks = false;
    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("N'") || token.startsWith("X'")
                || token.startsWith("DATE'") || token.startsWith("TIME'") || token.startsWith("TIMESTAMP'")) {
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

        // TODO: When a line contains a CREATE X and and END statement, then the order is significant.
        // TODO: There are a couple more statements allowing for nested begin/end blocks
        if (line.startsWith("CREATE PROCEDURE") || line.startsWith("CREATE TRIGGER")) {
            if (insideStatementAllowingNestedBeginEndBlocks) {
                throw new FlywayException("Beginning another CREATE TRIGGER/CREATE PROCEDURE inside Trigger or Procedure not supported");
            }
            insideStatementAllowingNestedBeginEndBlocks = true;
        }

        if (insideStatementAllowingNestedBeginEndBlocks) {
            if (line.startsWith("BEGIN")) {
                beginEndNestedDepth++;
            }

            if (line.endsWith("END;")) {
                beginEndNestedDepth--;
                if (beginEndNestedDepth < 0) {
                    throw new FlywayException("Syntax error in SQL script: unpaired END; statement");
                } else if (beginEndNestedDepth == 0) {
                    insideStatementAllowingNestedBeginEndBlocks = false;
                }
            }
        }

        if (insideStatementAllowingNestedBeginEndBlocks) {
            return null;
        }
        return getDefaultDelimiter();
    }
}
