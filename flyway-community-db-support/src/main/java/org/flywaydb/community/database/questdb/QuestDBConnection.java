/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.questdb;

import java.util.concurrent.Callable;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLConnection;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

public class QuestDBConnection extends PostgreSQLConnection {

    // QuestDB doesn't support schemas, everything is under qdb
    private final QuestDBSchema schema = new QuestDBSchema(jdbcTemplate, (QuestDBDatabase) database, "qdb");

    QuestDBConnection(QuestDBDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        return schema;
    }

    @Override
    protected void doRestoreOriginalState() {
    }

    @Override
    public Schema doGetCurrentSchema() {
        return schema;
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "qdb";
    }

    @Override
    public void changeCurrentSchemaTo(final Schema schema) {
        // QuestDB's postgres implementation is not really schema-aware. You connect to "qdb" but "schemas" are just string-prefixed table names, nothing more
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(final String schema) {
    }

    @Override
    public <T> T lock(final Table table, final Callable<T> callable) {
        return ExecutionTemplateFactory
                .createTableExclusiveExecutionTemplate(jdbcTemplate.getConnection(), table, database)
                .execute(callable);
    }
}