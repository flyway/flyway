/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core;

import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Abstraction for database-specific functionality.
 */
public interface DbSupport {
    /**
     * Generates the sql statements for creating the schema meta-data table.
     *
     * @param tableName The name to give to this table.
     * @return The sql statements.
     */
    String[] createSchemaMetaDataTableSql(String tableName);

    /**
     * Retrieves the current schema for this connection.
     *
     * @param connection The connection to check.
     * @return The current schema for this connection.
     */
    String getCurrentSchema(Connection connection) throws SQLException;

    /**
     * Checks whether this DbSupport class supports the database with this product name.
     *
     * @param databaseProductName The name of the database.
     * @return {@code true} if the database is supported, {@code false} if not.
     */
    boolean supportsDatabase(String databaseProductName);

    /**
     * Checks whether Flyway's metadata table is already present in the database.
     *
     * @param jdbcTemplate        The jdbc template used for querying the database.
     * @param schema              The schema to check.
     * @param schemaMetaDataTable The table to look for.
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     * @throws SQLException Thrown when the database metadata could not be read.
     */
    boolean metaDataTableExists(SimpleJdbcTemplate jdbcTemplate, String schema, String schemaMetaDataTable) throws SQLException;

    /**
     * Checks whether ddl transactions are supported for this database.
     *
     * @return {@code true} if ddl transactions are supported, {@code false} if not.
     */
    boolean supportsDdlTransactions();

    /**
     * Creates a new sql script from this resource with these placeholders to replace.
     *
     * @param resource     The resource containing the sql script.
     * @param placeholders A map of <placeholder, replacementValue> to replace in sql statements.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    SqlScript createSqlScript(Resource resource, Map<String, String> placeholders);
}
