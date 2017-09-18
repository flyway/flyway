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
package org.flywaydb.core.internal.dbsupport.snowflake;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.internal.dbsupport.*;

/**
 * Snowflake-specific table.
 */
public class SnowflakeTable extends Table {
    /**
     * Creates a new Snowflake table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public SnowflakeTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.executeStatement("DROP TABLE " + dbSupport.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        return;
    }

    /**
     * Checks whether the database contains a table matching these criteria.
     *
     * @param catalog    The catalog where the table resides. (optional)
     * @param schema     The schema where the table resides. (optional)
     * @param table      The name of the table. (optional)
     * @param tableTypes The types of table to look for (ex.: TABLE). (optional)
     * @return {@code true} if a matching table has been found, {@code false} if not.
     * @throws SQLException when the check failed.
     */
    @Override
    protected boolean exists(Schema catalog, Schema schema, String table, String... tableTypes) throws SQLException {

        boolean found;
        List<Map<String, String>> resultSet = jdbcTemplate.queryForList("SHOW TABLES LIKE '" + name + "' IN SCHEMA \"" + schema.getName() + "\"");
        List<String> tableNames = new ArrayList<String>();
        for (final Map<String, String> result : resultSet) {
            tableNames.add(result.get("name"));
        }

        found = !tableNames.isEmpty();

        return found;
    }

    /**
     * Checks whether the database contains a column matching these criteria.
     *
     * @param column The column to look for.
     * @return {@code true} if a matching column has been found, {@code false} if not.
     */
    @Override
    public boolean hasColumn(String column) {
        boolean found;
        List<String> tableNames;
        try {
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? " +
                "AND TABLE_NAME = ? " +
                "AND COLUMN_NAME = ? ";
            tableNames = jdbcTemplate.queryForStringList(sql, schema.getName(), this.getName(), column);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check whether table " + this + " has a column named " + column, e);
        }
        found = !tableNames.isEmpty();

        return found;
    }

}