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
package org.flywaydb.core.internal.dbsupport.vertica;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLSqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * SqlStatementBuilder supporting Vertica specific syntax.
 */
public class VerticaStatementBuilder extends PostgreSQLSqlStatementBuilder {

    /**
     * Are we currently inside a BEGIN END; block?
     */
    private boolean insideBeginEndBlock;

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (statementStart.startsWith("CREATE FUNCTION")
                || statementStart.startsWith("CREATE OR REPLACE FUNCTION")) {
            if (line.startsWith("BEGIN") || line.endsWith("BEGIN")) {
                insideBeginEndBlock = true;
            }

            if (line.endsWith("END;")) {
                insideBeginEndBlock = false;
            }
        }

        if (insideBeginEndBlock) {
            return null;
        }
        return getDefaultDelimiter();
    }

}
