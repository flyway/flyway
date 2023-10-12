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

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLSchema;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLTable;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class QuestDBSchema extends PostgreSQLSchema {

    public static final Set<String> IGNORED_TABLES = Set.of("sys.telemetry_wal", "telemetry", "telemetry_config", "sys.column_versions_purge_log");

    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param name The name of the schema.
     */
    public QuestDBSchema(JdbcTemplate jdbcTemplate, QuestDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    public Table getTable(String tableName) {
        return new QuestDBTable(jdbcTemplate, (QuestDBDatabase) database, this, tableName);
    }

    @Override
    protected boolean doExists() {
        return true;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return IGNORED_TABLES.containsAll(jdbcTemplate.queryForStringList("show tables"));
    }

    @Override
    protected void doCreate() {

    }

    @Override
    protected void doDrop() {

    }

    @Override
    protected void doClean() {
        for (Table table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected PostgreSQLTable[] doAllTables() throws SQLException {
        List<String> tableNames =
                jdbcTemplate.queryForStringList("SHOW TABLES").stream()
                        .filter(tbl -> !IGNORED_TABLES.contains(tbl))
                        .toList();
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        PostgreSQLTable[] tables = new PostgreSQLTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new QuestDBTable(jdbcTemplate, (QuestDBDatabase)database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected Type getType(final String typeName) {
        throw new RuntimeException(new OperationNotSupportedException("This should not be called"));
    }
}