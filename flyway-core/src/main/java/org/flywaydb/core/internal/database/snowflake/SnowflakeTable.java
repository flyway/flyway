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

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Snowflake-specific table.
 */
public class SnowflakeTable extends Table {
    /**
     * Creates a new Snowflake table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public SnowflakeTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
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
        List<HashMap<String, String>> tablesMetadata = getMetadataForObjectType("TABLES", name, "name");

        return tablesMetadata.size() > 0;
    }

    /**
     * Checks whether the database contains a column matching these criteria.
     *
     * @param column The column to look for.
     * @return {@code true} if a matching column has been found, {@code false} if not.
     */
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

    /**
     * Helper method to retrieve a result set of metadata using SHOW
     *
     * @param objectType The type of object to return metadata for (expects plural)
     * @param resultColumnNames Set of column names to select from the result set
     * @return A list of result set rows
     * @throws SQLException when the query execution failed.
     */
    private List<HashMap<String, String>> getMetadataForObjectType(String objectType, String objectFilter, String... resultColumnNames) throws SQLException {
        String inSchemaString;
        if (objectType != "SCHEMAS") {
            inSchemaString = " IN SCHEMA " + database.quote(schema.getName());
        }
        else {
            inSchemaString = "";
        }

        String metadataQuery = "SHOW " + objectType + " LIKE '" + objectFilter + "'" + inSchemaString;
        List<HashMap<String, String>> resultRows = jdbcTemplate.query(
                metadataQuery,
                new RowMapper<HashMap<String, String>>() {
                    @Override
                    public HashMap<String, String> mapRow(ResultSet rs) throws SQLException {
                        HashMap<String, String> objectList = new HashMap<String, String>();
                        for(String resultColumnName : resultColumnNames) {
                            objectList.put(resultColumnName, rs.getString(resultColumnName));
                        }
                        return objectList;
                    }
                });

        return resultRows;
    }
}
