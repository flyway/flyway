/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

/**
 * Interface to define new experimental database plugins.
 */
public sealed interface ExperimentalDatabase <T> extends Plugin, AutoCloseable permits AbstractExperimentalDatabase {
    Log LOG = org.flywaydb.core.api.logging.LogFactory.getLog(ExperimentalDatabase.class);
    String APPLICATION_NAME = "Flyway by Redgate";

    /**
     * Check for if this database type supports the provided URL/Connection String.
     * @param url URL or Connection String for the database being connected to. This will be obtained from a resolved environment.
     * @return A {@link org.flywaydb.core.experimental.DatabaseSupport] object containing confirmation that the database type is supported
     */
    DatabaseSupport supportsUrl(String url);

    default boolean canCreateJdbcDataSource() {
        return false;
    }

    default boolean isOnByDefault(final Configuration configuration) {
        return false;
    }

    List<String> supportedVerbs();

    boolean supportsTransactions();

    /**
     * To initialize the connection to the database. This function will vary between the connection types.
     * For example, a JDBC connection will establish a connection object.
     * However, an API connection may require this function to create an authentication object instead.
     * @param environment The resolved environment to connect to.
     */
    void initialize(ResolvedEnvironment environment, Configuration configuration);

    void doExecute(T executionUnit, final boolean outputQueryResults);

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
    void createSchemaHistoryTable(Configuration configuration);

    boolean schemaHistoryTableExists(String tableName);

    /**
     * Get a model representation of the schema history table and its content.
     * @param tableName The name of the schema history table.
     * @return A model representation of the schema history table and its content.
     */
    SchemaHistoryModel getSchemaHistoryModel(String tableName);
    
    void appendSchemaHistoryItem(SchemaHistoryItem item,  String tableName);


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

    String getDefaultSchema(Configuration configuration);

    /**
     * Checks if all schemas are empty.
     * @return True if all schemas are empty, false otherwise.
     */
    default Boolean allSchemasEmpty(String[] schemas) {
        for (String schema: schemas) {
            if (!isSchemaEmpty(schema)) {
                return false;
            }
        }
        return true;
    }

    boolean isSchemaEmpty(String schema);

    boolean isSchemaExists(String schema);

    void createSchemas(String... schemas);

    String getDatabaseType();

    BiFunction<Configuration, ParsingContext, Parser> getParser();

    void addToBatch(String executionUnit);

    /**
     * Executes the current batch against the database.
     */
    void doExecuteBatch();
    
    int getBatchSize();
        
    String getCurrentUser();

    void startTransaction();

    void commitTransaction();

    void rollbackTransaction();

    default void doClean(final List<String> schemas, final List<String> schemasToDrop, final CleanResult cleanResult) {
        final StopWatch watch = new StopWatch();
        for (final String schema : schemas) {
            try {
                watch.start();
                doCleanSchema(schema);
                watch.stop();
                LOG.info(String.format("Successfully cleaned schema %s (execution time %s)",
                    quote(schema),
                    TimeFormat.format(watch.getTotalTimeMillis())));
                if (!schemasToDrop.contains(schema)) {
                    cleanResult.schemasCleaned.add(schema);
                }
            } catch (final Exception ignored) {

            }
        }
        for (final String schema : schemasToDrop) {
            try {
                watch.start();
                doDropSchema(schema);
                watch.stop();
                LOG.info(String.format("Successfully dropped schema %s (execution time %s)",
                    quote(schema),
                    TimeFormat.format(watch.getTotalTimeMillis())));
                cleanResult.schemasDropped.add(schema);
            } catch (final FlywayException e) {
                LOG.debug(e.getMessage());
                LOG.warn("Unable to drop schema " + schema + ". It was cleaned instead.");
                cleanResult.schemasCleaned.add(schema);
            }
        }
    }

    void doCleanSchema(String schema);

    void doDropSchema(String schema);

    default String getInstalledBy(final Configuration configuration) {
        final String installedBy = configuration.getInstalledBy();
        return installedBy == null ? getCurrentUser() : installedBy;
    }

    default void createSchemaHistoryTableIfNotExists(final Configuration configuration) {
        if (!schemaHistoryTableExists(configuration.getTable())) {
            LOG.info("Creating Schema History table "
                + quote(getCurrentSchema(), configuration.getTable())
                + " ...");
            createSchemaHistoryTable(configuration);
        }
    }

    void removeFailedSchemaHistoryItems(final String tableName);

    void updateSchemaHistoryItem(SchemaHistoryItem item, final String tableName);

    default String redactUrl(final String url) {
        String redactedUrl = url;
        for (final Pattern pattern : getUrlRedactionPatterns()) {
            final Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                final String password = matcher.group(1);
                redactedUrl = redactedUrl.replace(password, "********");
            }
        }
        return redactedUrl;
    }

    default Pattern[] getUrlRedactionPatterns() {
        return new Pattern[] {Pattern.compile("password=([^;&]*).*", Pattern.CASE_INSENSITIVE),
                              Pattern.compile("(?:jdbc:)?[^:]+://[^:]+:([^@]+)@.*", Pattern.CASE_INSENSITIVE)};
    }
    
    boolean isClosed();
}
