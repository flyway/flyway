/**
 * Copyright 2010-2016 Boxfuse GmbH
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

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * SQLite implementation of Schema.
 */
public class SQLiteSchema extends Schema<SQLiteDbSupport> {
    private static final Log LOG = LogFactory.getLog(SQLiteSchema.class);

    /**
     * Creates a new SQLite schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public SQLiteSchema(JdbcTemplate jdbcTemplate, SQLiteDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
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
        return (tables.length == 0) || ((tables.length == 1) && "android_metadata".equals(tables[0].getName()));
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
        List<String> viewNames = jdbcTemplate.queryForStringList("SELECT tbl_name FROM " + dbSupport.quote(name) + ".sqlite_master WHERE type='view'");
        for (String viewName : viewNames) {
            jdbcTemplate.executeStatement("DROP VIEW " + dbSupport.quote(name, viewName));
        }

        for (Table table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("SELECT tbl_name FROM " + dbSupport.quote(name) + ".sqlite_master WHERE type='table'");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SQLiteTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SQLiteTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
