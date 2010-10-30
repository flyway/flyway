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

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;

/**
 * Abstraction for database-specific functionality.
 */
public interface DbSupport {
    /**
     * Creates a new sql script from this resource with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     *
     * @return A new sql script, containing the statements from this resource, with all placeholders replaced.
     *
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer);

    /**
     * Creates a new sql script which clean the current schema, by dropping all objects.
     *
     * @return A new sql script, containing drop statements for all objects
     */
    SqlScript createCleanScript();

    /**
     * Returns the location on the classpath where the scripts for this database reside.
     *
     * @return The folder on the classpath, including a trailing slash.
     */
    String getScriptLocation();

    /**
     * Checks whether this table is already present in the database.
     *
     * @param table The table to look for.
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    boolean tableExists(String table);

    /**
     * Checks whether this column is already present in this table in the database.
     *
     * @param table  The table to look for.
     * @param column The column to look for.
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    boolean columnExists(String table, String column);

    /**
     * Retrieves the current schema.
     *
     * @return The current schema for this connection.
     */
    String getCurrentSchema();

    /**
     * @return The database function that returns the current user.
     */
    String getCurrentUserFunction();

    /**
     * Checks whether ddl transactions are supported for this database.
     *
     * @return {@code true} if ddl transactions are supported, {@code false} if not.
     */
    boolean supportsDdlTransactions();

    /**
     * Checks whether locking using select ... for update is supported for this database.
     *
     * @return {@code true} if locking is supported, {@code false} if not.
     */
    boolean supportsLocking();
}
