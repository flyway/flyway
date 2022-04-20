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
package org.flywaydb.core.internal.database.snowflake;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.flywaydb.core.internal.util.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@CustomLog
public class SnowflakeSchema extends Schema<SnowflakeDatabase, SnowflakeTable> {
    /**
     * Creates a new Snowflake schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param name The name of the schema.
     */
    SnowflakeSchema(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        List<Boolean> results = jdbcTemplate.query("SHOW SCHEMAS LIKE '" + name + "'", rs -> true);
        return !results.isEmpty();
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = getObjectCount("TABLE") + getObjectCount("VIEW")
                + getObjectCount("SEQUENCE");

        return objectCount == 0;
    }

    private int getObjectCount(String objectType) throws SQLException {
        return jdbcTemplate.query("SHOW " + objectType + "S IN SCHEMA " + database.quote(name), rs -> 1).size();
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String dropStatement : generateDropStatements("VIEW")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("SEQUENCE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Pair<String, String> snowflakeDropPair : generateDropStatementsWithArgs("USER FUNCTIONS", "FUNCTION")) {
            try {
                jdbcTemplate.execute(snowflakeDropPair.getRight());
            } catch (SQLException sqlException) {
                LOG.warn("Unable to drop User Function " + snowflakeDropPair.getLeft() + ": " + sqlException.getMessage());
            }
        }

        for (Pair<String, String> snowflakeDropPair : generateDropStatementsWithArgs("PROCEDURES", "PROCEDURE")) {
            try {
                jdbcTemplate.execute(snowflakeDropPair.getRight());
            } catch (SQLException sqlException) {
                LOG.warn("Unable to drop Procedure " + snowflakeDropPair.getLeft() + ": " + sqlException.getMessage());
            }
        }
    }

    @Override
    protected SnowflakeTable[] doAllTables() throws SQLException {
        List<SnowflakeTable> tables = jdbcTemplate.query("SHOW TABLES IN SCHEMA " + database.quote(name), new RowMapper<SnowflakeTable>() {
            @Override
            public SnowflakeTable mapRow(ResultSet rs) throws SQLException {
                String tableName = rs.getString("name");
                return (SnowflakeTable) getTable(tableName);
            }
        });
        return tables.toArray(new SnowflakeTable[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new SnowflakeTable(jdbcTemplate, database, this, tableName);
    }

    private List<String> generateDropStatements(final String objectType) throws SQLException {
        return jdbcTemplate.query("SHOW " + objectType + "S IN SCHEMA " + database.quote(name), rs -> {
            String tableName = rs.getString("name");
            return "DROP " + objectType + " " + database.quote(name) + "." + database.quote(tableName);
        });
    }

    private List<Pair<String,String>> generateDropStatementsWithArgs(final String showObjectType, final String dropObjectType) throws SQLException {
        return jdbcTemplate.query("SHOW " + showObjectType + " IN SCHEMA " + database.quote(name), rs -> {
            String nameAndArgsList = rs.getString("arguments");
            int indexOfEndOfArgs = nameAndArgsList.indexOf(") RETURN ");
            String functionName = nameAndArgsList.substring(0, indexOfEndOfArgs + 1);
            return Pair.of(functionName, "DROP " + dropObjectType + " " + name + "." + functionName);
        });
    }
}