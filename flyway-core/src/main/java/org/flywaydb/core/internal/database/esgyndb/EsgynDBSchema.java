/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.esgyndb;

import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EsgynDB implementation of Schema.
 */
public class EsgynDBSchema extends Schema<EsgynDBDatabase, EsgynDBTable> {
    /**
     * Creates a new EsgynDB schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    public EsgynDBSchema(JdbcTemplate jdbcTemplate, EsgynDBDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    /**
     * In the following queries hardcoding catalog TRAFODION
     * for efficient metadata access. If HIVE supported, will need to change this.
     */

    /**
     * Checks whether this schema exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     * @throws SQLException when the check failed.
     */
    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*)\n" +
                "   FROM \"_MD_\".OBJECTS\n" +
				"  WHERE CATALOG_NAME = 'TRAFODION'\n" +
                "    AND OBJECT_TYPE IN('PS','SS')\n" +
                "    AND SCHEMA_NAME = ?", name) > 0;
    }

    /**
     * Checks whether this schema is empty.
     *
     * @return {@code true} if it is, {@code false} if isn't.
     * @throws SQLException when the check failed.
     */
    @Override
    protected boolean doEmpty() throws SQLException {
        return !jdbcTemplate.queryForBoolean("SELECT\n" +
                "		CASE COUNT(*) WHEN 0 THEN TRUE ELSE FALSE END\n" +
                "   FROM \"_MD_\".OBJECTS\n" +
                "  WHERE CATALOG_NAME = 'TRAFODION'\n" +
                "    AND OBJECT_TYPE NOT IN ('PS','SS')\n" +
                "    AND SCHEMA_NAME = ?\n" +
                "    AND NOT (OBJECT_NAME LIKE 'SB_HISTOGRAM%'\n" +
                "         OR  OBJECT_NAME LIKE 'SB_PERSISTENT%')", name);
    }

    /**
     * Creates this schema in the database.
     */
    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA TRAFODION." + database.quote(name));
    }

    /**
     * Drops this schema from the database.
     *
     * @throws SQLException when the drop failed.
     */
    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA TRAFODION." + database.quote(name) + " CASCADE");
    }

    /**
     * Cleans all the objects in this schema.
     *
     * @throws SQLException when the clean failed.
     */
    @Override
    protected void doClean() throws SQLException {
        doDrop();
		doCreate();
    }

    /**
     * Retrieves all the tables in this schema.
     *
     * @return All tables in the schema.
     * @throws SQLException when the retrieval failed.
     */
    private EsgynDBTable[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        EsgynDBTable[] tables = new EsgynDBTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new EsgynDBTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected EsgynDBTable[] doAllTables() throws SQLException {
        return findTables("SELECT OBJECT_NAME\n" +
               "   FROM \"_MD_\".OBJECTS\n" +
               "  WHERE CATALOG_NAME = 'TRAFODION'\n" +
               "    AND OBJECT_TYPE = 'BT'\n" +
               "    AND SCHEMA_NAME = ?\n" +
               "    AND NOT (OBJECT_NAME LIKE 'SB_HISTOGRAM%'\n" +
               "         OR  OBJECT_NAME LIKE 'SB_PERSISTENT%')", name);
    }

    /**
     * Retrieves the table with this name in this schema.
     *
     * @param tableName The name of the table.
     * @return The table.
     */
    @Override
    public Table getTable(String tableName) {
        return new EsgynDBTable(jdbcTemplate, database, this, tableName);
    }

    /**
     * Retrieves all the functions in this schema.
     *
     * @return All functions in the schema.
     * @throws SQLException when the retrieval failed.
     */
    @Override
    protected Function[] doAllFunctions() throws SQLException {
        List<String> functionNames = jdbcTemplate.queryForStringList(
        	   "SELECT OBJECT_NAME\n" +
        	   "  FROM \"_MD_\".OBJECTS\n" +
        	   " WHERE CATALOG_NAME = 'TRAFODION'\n" +
        	   "   AND OBJECT_TYPE = 'UR'\n" +
        	   "   AND SCHEMA_NAME = ?", name);

        List<Function> functions = new ArrayList<>();
        for (String functionName : functionNames) {
            functions.add(getFunction(functionName));
        }

        return functions.toArray(new Function[0]);
    }

    /**
     * Retrieves the function with this name in this schema.
     *
     * @param functionName The name of the function.
     * @return The function.
     */
    @Override
    public Function getFunction(String functionName, String... args) {
        return new EsgynDBFunction(jdbcTemplate, database, this, functionName, args);
    }

}