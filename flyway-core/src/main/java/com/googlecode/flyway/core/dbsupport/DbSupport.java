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

package com.googlecode.flyway.core.dbsupport;

import com.googlecode.flyway.core.runtime.SqlScript;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * Abstraction for database-specific functionality.
 */
public interface DbSupport {
    /**
     * Creates a new sql script from this resource with these placeholders to
     * replace.
     *
     * @param sqlScriptSource The sql script as a text block with all placeholders still present.
     * @param placeholders    A map of <placeholder, replacementValue> to replace in sql
     *                        statements.
     * @return A new sql script, containing the statements from this resource,
     *         with all placeholders replaced.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    SqlScript createSqlScript(String sqlScriptSource, Map<String, String> placeholders);

    /**
     * Creates a new sql script which clean the current schema, by dropping all objects.
     *
     * @param jdbcTemplate The jdbc template used for querying the database.
     * @return A new sql script, containing drop statements for all objects
     */
    SqlScript createCleanScript(JdbcTemplate jdbcTemplate);

    /**
     * @return The location on the classpath where the create metadata table script resides.
     */
    String getCreateMetaDataTableScriptLocation();

    /**
     * Checks whether Flyway's metadata table is already present in the
     * database.
     *
     * @param jdbcTemplate        The jdbc template used for querying the database.
     * @param schemaMetaDataTable The table to look for.
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    boolean metaDataTableExists(JdbcTemplate jdbcTemplate, String schemaMetaDataTable);

    /**
     * Retrieves the current schema.
     *
     * @param jdbcTemplate The jdbc template used for querying the database.
     * @return The current schema for this connection.
     */
    String getCurrentSchema(JdbcTemplate jdbcTemplate);

    /**
     * Checks whether this DbSupport class supports the database with this
     * product name.
     *
     * @param databaseProductName The name of the database.
     * @return {@code true} if the database is supported, {@code false} if not.
     */
    boolean supportsDatabase(String databaseProductName);

    /**
     * Checks whether ddl transactions are supported for this database.
     *
     * @return {@code true} if ddl transactions are supported, {@code false} if
     *         not.
     */
    boolean supportsDdlTransactions();

    /**
     * Checks whether locking using select ... for update is supported for this
     * database.
     *
     * @return {@code true} if locking is supported, {@code false} if not.
     */
    boolean supportsLocking();
}
