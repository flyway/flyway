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

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * SqlStatementBuilder supporting SAP HANA-specific delimiter changes.
 */
public class SapHanaSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Are we currently inside a BEGIN END; block?
     */
    private boolean insideBeginEndBlock;

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

        if (statementStart.startsWith("CREATE TRIGGER")) {
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
        return getDefaultDelimiter();
    }
}
