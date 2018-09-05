/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQLite implementation of Schema.
 */
public class SQLiteSchema extends Schema<SQLiteDatabase> {
    private static final Log LOG = LogFactory.getLog(SQLiteSchema.class);

    private static final List<String> IGNORED_SYSTEM_TABLE_NAMES =
            Arrays.asList("android_metadata", SQLiteTable.SQLITE_SEQUENCE);

    /**
     * Creates a new SQLite schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    SQLiteSchema(JdbcTemplate jdbcTemplate, SQLiteDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        try {
            doAllTables();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        Table[] tables = allTables();
        List<String> tableNames = new ArrayList<>();
        for (Table table : tables) {
            String tableName = table.getName();
            if (!IGNORED_SYSTEM_TABLE_NAMES.contains(tableName)) {
                tableNames.add(tableName);
            }
        }
        return tableNames.isEmpty();
    }

    @Override
    protected void doCreate() throws SQLException {
        LOG.info("SQLite does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        LOG.info("SQLite does not support dropping schemas. Schema not dropped: " + name);
    }

    @Override
    protected void doClean() throws SQLException {
        List<String> viewNames = jdbcTemplate.queryForStringList("SELECT tbl_name FROM " + database.quote(name) + ".sqlite_master WHERE type='view'");
        for (String viewName : viewNames) {
            jdbcTemplate.execute("DROP VIEW " + database.quote(name, viewName));
        }

        for (Table table : allTables()) {
            table.drop();
        }

        if (getTable(SQLiteTable.SQLITE_SEQUENCE).exists()) {
            jdbcTemplate.execute("DELETE FROM " + SQLiteTable.SQLITE_SEQUENCE);
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("SELECT tbl_name FROM " + database.quote(name) + ".sqlite_master WHERE type='table'");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SQLiteTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SQLiteTable(jdbcTemplate, database, this, tableName);
    }
}