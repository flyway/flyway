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
package org.flywaydb.core.internal.dbsupport.clickhouse;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * ClickHouse implementation of Schema.
 */
public class ClickHouseSchema extends Schema<ClickHouseDbSupport> {

    /**
     * Creates a new schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public ClickHouseSchema(JdbcTemplate jdbcTemplate, ClickHouseDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM system.databases WHERE name = ?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM system.tables WHERE database = ?", name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.executeStatement("CREATE DATABASE " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        if (jdbcTemplate.getConnection().getCatalog().equals(name))
            jdbcTemplate.getConnection().setCatalog("default");
        jdbcTemplate.executeStatement("DROP DATABASE " + dbSupport.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (Table table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("SELECT name FROM system.tables WHERE database = ?", name);
        Table[] result = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++)
            result[i] = new ClickHouseTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        return result;
    }

    @Override
    public Table getTable(String tableName) {
        return new ClickHouseTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
