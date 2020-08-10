/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DefaultExecutionStrategy;
import org.flywaydb.core.internal.jdbc.*;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.sqlscript.*;

import java.sql.Connection;
import java.sql.*;
import java.util.Properties;

import static org.flywaydb.core.internal.sqlscript.SqlScriptMetadata.getMetadataResource;

public abstract class DatabaseType {
    protected static final Log LOG = LogFactory.getLog(DatabaseType.class);

    /**
     * The name of the application that created the connection. This is useful for databases that allow setting this
     * in order to easily correlate individual application with database connections.
     */
    protected static final String APPLICATION_NAME = "Flyway by Redgate";

    protected final ClassLoader classLoader;

    public DatabaseType(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @return The human-readable name for this database.
     */
    public abstract String getName();

    /**
     * @return The JDBC type used to represent {@code null} in prepared statements.
     */
    public abstract int getNullType();











    /**
     * Check if this database type should handle the given JDBC url
     * @param url The JDBC url.
     * @return {@code true} if this handles the JDBC url, {@code false} if not.
     */
    public abstract boolean handlesJDBCUrl(String url);

    /**
     * Get the driver class used to handle this JDBC url.
     * This will only be called if {@code matchesJDBCUrl} previously returned {@code true}.
     * @param url The JDBC url.
     * @return The full driver class name to be instantiated to handle this url.
     */
    public abstract String getDriverClass(String url);

    /**
     * Retrieves a second choice backup driver for a JDBC url, in case the one returned by {@code getDriverClass} is not available.
     *
     * @param url The JDBC url.
     * @return The JDBC driver. {@code null} if none.
     */
    public String getBackupDriverClass(String url) {
        return null;
    }

    /**
     * Check if this database type handles the connection product name and version.
     * This allows more fine-grained control over which DatabaseType handles which connection.
     *
     * @param databaseProductName The product name returned by the database.
     * @param databaseProductVersion The product version returned by the database.
     * @param connection The connection used to connect to the database.
     * @return {@code true} if this handles the product name and version, {@code false} if not.
     */
    public abstract boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection);

    /**
     * Initializes the Database class, and optionally prints some information.
     *
     * @param configuration The Flyway configuration.
     * @param jdbcConnectionFactory The current connection factory.
     * @param printInfo     Where the DB info should be printed in the logs.
     * @return The appropriate Database class.
     */
    public Database createDatabase(
            Configuration configuration, boolean printInfo,
            JdbcConnectionFactory jdbcConnectionFactory



    ) {
        String databaseProductName = jdbcConnectionFactory.getProductName();
        if (printInfo) {
            LOG.info("Database: " + jdbcConnectionFactory.getJdbcUrl() + " (" + databaseProductName + ")");
            LOG.debug("Driver  : " + jdbcConnectionFactory.getDriverInfo());
        }

        Database database = createDatabase(configuration, jdbcConnectionFactory



        );

        String intendedCurrentSchema = configuration.getDefaultSchema();
        if (!database.supportsChangingCurrentSchema() && intendedCurrentSchema != null) {
            LOG.warn(databaseProductName + " does not support setting the schema for the current session. " +
                    "Default schema will NOT be changed to " + intendedCurrentSchema + " !");
        }

        return database;
    }

    /**
     * Initializes the Database used by this Database Type.
     *
     * @param configuration The Flyway configuration.
     * @param jdbcConnectionFactory The current connection factory.
     * @return The Database.
     */
    public abstract Database createDatabase(
            Configuration configuration,
            JdbcConnectionFactory jdbcConnectionFactory



    );

    /**
     * Initializes the Parser used by this Database Type.
     *
     * @param configuration The Flyway configuration.
     * @return The Parser.
     */
    public abstract Parser createParser(
            Configuration configuration



            , ParsingContext parsingContext
    );

    /**
     * Initializes the SqlScriptFactory used by this Database Type.
     *
     * @param configuration The Flyway configuration.
     * @return The SqlScriptFactory.
     */
    public SqlScriptFactory createSqlScriptFactory(
            final Configuration configuration,
            final ParsingContext parsingContext) {

        return new SqlScriptFactory() {
            @Override
            public SqlScript createSqlScript(LoadableResource resource, boolean mixed, ResourceProvider resourceProvider) {
                return new ParserSqlScript(createParser(configuration



                        , parsingContext
                ), resource, getMetadataResource(resourceProvider, resource), mixed);
            }
        };
    }

    /**
     * Initializes the SqlScriptExecutorFactory used by this Database Type.
     *
     * @param jdbcConnectionFactory The current connection factory.
     * @return The SqlScriptExecutorFactory.
     */
    public SqlScriptExecutorFactory createSqlScriptExecutorFactory(
            final JdbcConnectionFactory jdbcConnectionFactory




    ) {




        final DatabaseType thisRef = this;

        return new SqlScriptExecutorFactory() {
            @Override
            public SqlScriptExecutor createSqlScriptExecutor(java.sql.Connection connection



            ) {
                return new DefaultSqlScriptExecutor(new JdbcTemplate(connection, thisRef)



                );
            }
        };
    }

    /**
     * Initializes the DatabaseExecutionStrategy used by this Database Type.
     *
     * @return The DatabaseExecutionStrategy.
     */
    public DatabaseExecutionStrategy createExecutionStrategy(java.sql.Connection connection) {
        return new DefaultExecutionStrategy();
    }

    /**
     * Initializes the ExecutionTemplate used by this Database Type.
     *
     * @return The ExecutionTemplate.
     */
    public ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        return new TransactionalExecutionTemplate(connection, rollbackOnException);
    }

    /**
     * Retrieves the version string for this connection as described by SELECT VERSION(), which may differ
     * from the connection metadata.
     *
     * @param connection The connection to use.
     * @return The version string.
     */
    public static String getSelectVersionOutput(Connection connection) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = connection.prepareStatement("SELECT version()");
            resultSet = statement.executeQuery();
            result = null;
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            return "";
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    /**
     * Detect the default connection properties for this database.
     *
     * @param url The JDBC url.
     * @param props The properties to write to.
     */
    public void setDefaultConnectionProps(String url, Properties props) {
        return;
    }

    /**
     * Shutdown the database that was opened (only applicable to embedded databases that require this).
     *
     * @param url The JDBC url used to create the database.
     * @param driver The driver created for the url.
     */
    public void shutdownDatabase(String url, Driver driver) {
        return;
    }

    /**
     * Detects whether a user is required from configuration. This may not be the case if the driver supports
     * other authentication mechanisms, or supports the user being encoded in the URL
     *
     * @param url The url to check
     * @return false if a username needs to be provided
     */
    public boolean detectUserRequiredByUrl(String url) {
        return true;
    }

    /**
     * Detects whether a password is required from configuration. This may not be the case if the driver supports
     * other authentication mechanisms, or supports the password being encoded in the URL
     *
     * @param url The url to check
     * @return false if a username needs to be provided
     */
    public boolean detectPasswordRequiredByUrl(String url) {
        return true;
    }
}