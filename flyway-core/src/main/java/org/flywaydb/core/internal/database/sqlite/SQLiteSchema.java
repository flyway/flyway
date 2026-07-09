/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.database.sqlite;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CustomLog
public class SQLiteSchema extends Schema<SQLiteDatabase, SQLiteTable> {
    private static final List<String> IGNORED_SYSTEM_TABLE_NAMES = Arrays.asList("android_metadata",
        SQLiteTable.SQLITE_SEQUENCE);

    private boolean foreignKeysEnabled;

    SQLiteSchema(final JdbcTemplate jdbcTemplate, final SQLiteDatabase database, final String name) {
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
    protected boolean doEmpty() {
        final Table[] tables = allTables();
        final List<String> tableNames = new ArrayList<>();
        for (final Table table : tables) {
            final String tableName = table.getName();
            if (!IGNORED_SYSTEM_TABLE_NAMES.contains(tableName)) {
                tableNames.add(tableName);
            }
        }
        return tableNames.isEmpty();
    }

    @Override
    protected void doCreate() {
        LOG.info("SQLite does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() {
        LOG.info("SQLite does not support dropping schemas. Schema not dropped: " + name);
    }

    @Override
    protected void doClean() throws SQLException {
        foreignKeysEnabled = jdbcTemplate.queryForBoolean("PRAGMA foreign_keys");

        final List<String> viewNames = jdbcTemplate.queryForStringList("SELECT tbl_name FROM "
            + database.quote(name)
            + ".sqlite_master WHERE type='view'");

        for (final String viewName : viewNames) {
            jdbcTemplate.execute("DROP VIEW " + database.quote(name, viewName));
        }

        for (final Table table : allTables()) {
            table.drop();
        }

        if (getTable(SQLiteTable.SQLITE_SEQUENCE).exists()) {
            jdbcTemplate.execute("DELETE FROM " + SQLiteTable.SQLITE_SEQUENCE);
        }
    }

    @Override
    protected SQLiteTable[] doAllTables() throws SQLException {
        final List<String> tableNames = jdbcTemplate.queryForStringList("SELECT tbl_name FROM "
            + database.quote(name)
            + ".sqlite_master WHERE type='table'");

        final SQLiteTable[] tables = new SQLiteTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SQLiteTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(final String tableName) {
        return new SQLiteTable(jdbcTemplate, database, this, tableName);
    }

    public boolean getForeignKeysEnabled() {
        return foreignKeysEnabled;
    }
}
