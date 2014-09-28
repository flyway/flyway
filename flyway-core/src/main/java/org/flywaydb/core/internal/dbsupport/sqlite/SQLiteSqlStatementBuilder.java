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
package org.flywaydb.core.internal.dbsupport.sqlite;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting H2-specific delimiter changes.
 */
public class SQLiteSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Are we inside a BEGIN block.
     */
    private boolean insideBeginEndBlock;

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.contains("BEGIN")) {
            insideBeginEndBlock = true;
        }

        if (line.endsWith("END;")) {
            insideBeginEndBlock = false;
        }

        if (insideBeginEndBlock) {
            return null;
        }
        return getDefaultDelimiter();
    }
}
