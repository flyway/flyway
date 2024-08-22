/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.experimental;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

/**
 * Interface to define new experimental database plugins.
 */
public interface ExperimentalDatabase extends Plugin, AutoCloseable  {

    /**
     * Check for if this database type supports the provided URL/Connection String.
     * @param url URL or Connection String for the database being connected to. This will be obtained from a resolved environment.
     * @return A {@link org.flywaydb.core.experimental.DatabaseSupport] object containing confirmation that the database type is supported
     */
    DatabaseSupport supportsUrl(String url);

    /**
     * To initialize the connection to the database. This function will vary between the connection types.
     * For example, a JDBC connection will establish a connection object.
     * However, an API connection may require this function to create an authentication object instead.
     * @param environment The resolved environment to connect to.
     */
    void initialize(ResolvedEnvironment environment, Configuration configuration) throws SQLException;

    /**
     * Gets connection important metadata from the database.
     * This metadata will be used primarily to confirm if the current database connection is right for the database variant connected to.
     * This is based off existing Flyway logic.
     * @return A {@link MetaData} object containing connection important metadata
     */
    MetaData getDatabaseMetaData();

    /**
     * Creates a schema history table against the configured database.
     * The implementation details will be determined per database but will adhere to a Flyway standard.
     * @param tableName The name of the schema history table to create.
     */
    void createSchemaHistoryTable(String tableName);

    boolean schemaHistoryTableExists(String tableName);

    /**
     * Get a model representation of the schema history table and its content.
     * @param tableName The name of the schema history table.
     * @return A model representation of the schema history table and its content.
     */
    SchemaHistoryModel getSchemaHistoryModel(String tableName);


    /**
     * Quotes this identifier for use in SQL queries.
     */
    default String doQuote(final String identifier) {
        return getOpenQuote() + identifier + getCloseQuote();
    }

    
    default String getOpenQuote() {
        return "\"";
    }

    
    default String getCloseQuote() {
        return "\"";
    }

    /**
     * Quotes these identifiers for use in SQL queries. Multiple identifiers will be quoted and separated by a dot.
     */
    default String quote(String... identifiers) {
        final StringBuilder result = new StringBuilder();

        boolean first = true;
        for (final String identifier : identifiers) {
            if (!first) {
                result.append(".");
            }
            first = false;
            result.append(doQuote(identifier));
        }

        return result.toString();
    }
    
    String getCurrentSchema();

    /**
     * Checks if all schemas are empty.
     * @return True if all schemas are empty, false otherwise.
     */
    Boolean allSchemasEmpty(String[] schemas);

    boolean isSchemaExists(String schema);
}
