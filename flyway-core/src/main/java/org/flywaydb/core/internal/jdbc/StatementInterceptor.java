/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface StatementInterceptor extends Plugin {
    void init(Configuration configuration, Database database, Table table);

    boolean isConfigured(Configuration configuration);

    List<Callback> getCallbacks();

    Connection createConnectionProxy(Connection connection);

    SchemaHistory getSchemaHistory(Configuration configuration, SchemaHistory jdbcTableSchemaHistory);

    void schemaHistoryTableCreate(boolean baseline);

    void schemaHistoryTableInsert(AppliedMigration appliedMigration);

    void close();

    void sqlScript(LoadableResource resource);

    void scriptMigration(LoadableResource resource);

    void javaMigration(JavaMigration javaMigration);

    void sqlStatement(SqlStatement statement);

    void interceptCommand(String command);

    void interceptStatement(String sql);

    void interceptPreparedStatement(String sql, Map<Integer, Object> params);

    void interceptCallableStatement(String sql);

    void schemaHistoryTableDeleteFailed(Table table, AppliedMigration appliedMigration);
}