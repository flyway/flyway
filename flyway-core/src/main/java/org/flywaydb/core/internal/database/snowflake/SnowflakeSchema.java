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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.flywaydb.core.internal.database.snowflake.SnowflakeObjectType.*;

/**
 * Snowflake implementation of Flyway Schema.
 */
public class SnowflakeSchema extends Schema<SnowflakeDatabase> {

    private static final Log LOG = LogFactory.getLog(SnowflakeSchema.class);

    private final String catalogName;

    public SnowflakeSchema(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, String name) {
        super(jdbcTemplate, database, name);
        LOG.debug("Creating new SnowflakeSchema");
        try {
            catalogName = jdbcTemplate.queryForString("SELECT CURRENT_DATABASE()");
            LOG.debug("Current Snowflake database is " + catalogName);
        } catch (SQLException e) {
            LOG.error("Unable to get current Snowflake database");
            throw new FlywaySqlException("Unable to get current database", e);
        }
    }

    //
    // NOTE: the following are overridden to implement
    //

    @Override
    protected boolean doExists() throws SQLException {
        List<Map<String, String>> objects = getObjects(SCHEMAS, name, "name");
        return objects.size() > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        if (doExists()) {
            List<Map<String, String>> objects = getObjects(OBJECTS, "%", "name");
            return objects.size() == 0;
        }
        else {
            return true;
        }
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(catalogName, name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(catalogName, name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : generateDropStatements(VIEWS)) {
            jdbcTemplate.execute(statement);
        }
        for (Table table : allTables()) {
            table.drop();
        }
        for (String statement : generateDropStatements(STAGES)) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements(FILE_FORMATS)) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements(SEQUENCES)) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements(PIPES)) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropFunctionStatements()) {
            jdbcTemplate.execute(statement);
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<Map<String, String>> objects = getObjects(TABLES, "%", "name");
        List<Table> tables = new ArrayList<>(objects.size());
        for (Map<String, String> object : objects) {
            String tableName = object.get("name");
            tables.add(getTable(tableName));
        }
        return tables.toArray(new Table[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new SnowflakeTable(jdbcTemplate, database, this, tableName);
    }

    //
    // NOTE: the following overrides are to change the implementation
    //

    //
    // Helper functions...
    //

    /* package */ List<Map<String, String>> getObjects(SnowflakeObjectType type, String filter, String... columns) throws SQLException {
        String inClause;
        if (type == SCHEMAS) {
            inClause = " IN DATABASE " + database.quote(catalogName);
        }
        else {
            inClause = " IN SCHEMA " + database.quote(catalogName, name);
        }

        String sql = "SHOW " + type.getShowType() + " LIKE '" + filter + "'" + inClause;
        LOG.debug("Executing [" + sql + "]");
        RowMapper<Map<String, String>> mapper = new RowMapper<Map<String, String>>() {

            @Override
            public Map<String, String> mapRow(ResultSet rs) throws SQLException {
                Map<String, String> result = new HashMap<String, String>();
                for (String column : columns) {
                    result.put(column, rs.getString(column));
                }
                return result;
            }
        };
        return jdbcTemplate.query(sql, mapper);
    }

    private List<String> generateDropStatements(SnowflakeObjectType type) throws SQLException {
        List<Map<String, String>> objects = getObjects(type, "%","name");
        List<String> result = new ArrayList<String>();
        for (Map<String, String> object : objects) {
            String value = object.get("name");
            result.add("DROP " + type.getCreateDropType() + " " + database.quote(name, value));
        }
        return result;
    }

    private List<String> generateDropFunctionStatements() throws SQLException {
        List<Map<String, String>> objects = getObjects(FUNCTIONS, "%","arguments");
        List<String> result = new ArrayList<String>();
        for (Map<String, String> object : objects) {
            String value = object.get("arguments");
            // remove the RETURN clause from the fuction signature
            value = value.replaceAll("\\sRETURN\\s.*", "");
            result.add("DROP " + FUNCTIONS.getCreateDropType() + " " + database.quote(name, value));
        }
        return result;
    }
}
