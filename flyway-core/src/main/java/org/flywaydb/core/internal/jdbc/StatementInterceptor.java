/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

import java.util.Map;

public interface StatementInterceptor {

    void init(Database database, Table table);
    void schemaHistoryTableCreate(boolean baseline);
    void schemaHistoryTableInsert(AppliedMigration appliedMigration);
    void close();
    void sqlScript(LoadableResource resource);
    void sqlStatement(SqlStatement statement);

    void interceptCommand(String command);
    void interceptStatement(String sql);
    void interceptPreparedStatement(String sql, Map<Integer, Object> params);
    void interceptCallableStatement(String sql);
    void schemaHistoryTableDeleteFailed(Table table, AppliedMigration appliedMigration);
}