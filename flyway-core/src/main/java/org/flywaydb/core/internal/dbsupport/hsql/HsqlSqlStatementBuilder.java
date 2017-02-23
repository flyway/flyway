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
package org.flywaydb.core.internal.dbsupport.hsql;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting Hsql-specific delimiter changes.
 */
public class HsqlSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Are we inside a BEGIN ATOMIC block.
     */
    private boolean insideAtomicBlock;

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.contains("BEGIN ATOMIC")) {
            insideAtomicBlock = true;
        }

        if (line.endsWith("END;")) {
            insideAtomicBlock = false;
        }

        if (insideAtomicBlock) {
            return null;
        }
        return getDefaultDelimiter();
    }
}
