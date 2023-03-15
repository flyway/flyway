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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public interface DatabaseType extends Plugin {
    /**
     * @return The human-readable name for this database type.
     */
    String getName();

    /**
     * @return The JDBC type used to represent {@code null} in prepared statements.
     */
    int getNullType();

    /**
     * Checks whether read-only transactions are supported by this database.
     *
     * @return {@code true} if read-only transactions are supported, {@code false} if not.
     */
    boolean supportsReadOnlyTransactions();

    /**
     * Check if this database type should handle the given JDBC url
     *
     * @param url The JDBC url.
     * @return {@code true} if this handles the JDBC url, {@code false} if not.
     */
    boolean handlesJDBCUrl(String url);

    /**
     * A regex that identifies credentials in the JDBC URL, where they conform to a pattern specific to this database.
     * The first captured group should represent the password text, so that it can be redacted if necessary.
     *
     * @return The URL regex.
     */
    Pattern getJDBCCredentialsPattern();

    /**
     * Get the driver class used to handle this JDBC url.
     * This will only be called if {@code matchesJDBCUrl} previously returned {@code true}.
     *
     * @param url The JDBC url.
     * @param classLoader The classLoader to check for driver classes.
     * @return The full driver class name to be instantiated to handle this url.
     */
    String getDriverClass(String url, ClassLoader classLoader);

    /**
     * Retrieves a second choice backup driver for a JDBC url, in case the one returned by {@code getDriverClass} is not available.
     *
     * @param url The JDBC url.
     * @param classLoader The classLoader to check for driver classes.
     * @return The JDBC driver. {@code null} if none.
     */
    String getBackupDriverClass(String url, ClassLoader classLoader);

    /**
     * Check if this database type handles the connection product name and version.
     * This allows more fine-grained control over which DatabaseType handles which connection.
     * Flyway will use the first DatabaseType that returns true for this method.
     *
     * @param databaseProductName The product name returned by the database.
     * @param databaseProductVersion The product version returned by the database.
     * @param connection The connection used to connect to the database.
     * @return {@code true} if this handles the product name and version, {@code false} if not.
     */
    boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection);

    /**
     * Initializes the Database class, and optionally prints some information.
     *
     * @param configuration The Flyway configuration.
     * @param jdbcConnectionFactory The current connection factory.
     * @param printInfo Where the DB info should be printed in the logs.
     * @return The appropriate Database class.
     */
    Database createDatabase(
            Configuration configuration, boolean printInfo,
            JdbcConnectionFactory jdbcConnectionFactory,
            StatementInterceptor statementInterceptor
                           );

    /**
     * Initializes the Database used by this Database Type.
     *
     * @param configuration The Flyway configuration.
     * @param jdbcConnectionFactory The current connection factory.
     * @return The Database.
     */
    Database createDatabase(
            Configuration configuration,
            JdbcConnectionFactory jdbcConnectionFactory,
            StatementInterceptor statementInterceptor
                           );

    /**
     * Initializes the Parser used by this Database Type.
     *
     * @param configuration The Flyway configuration.
     * @return The Parser.
     */
    Parser createParser(
            Configuration configuration
            , ResourceProvider resourceProvider
            , ParsingContext parsingContext
                       );

    /**
     * Initializes the SqlScriptFactory used by this Database Type.
     *
     * @param configuration The Flyway configuration.
     * @return The SqlScriptFactory.
     */
    SqlScriptFactory createSqlScriptFactory(
            final Configuration configuration,
            final ParsingContext parsingContext);

    /**
     * Initializes the SqlScriptExecutorFactory used by this Database Type.
     *
     * @param jdbcConnectionFactory The current connection factory.
     * @return The SqlScriptExecutorFactory.
     */
    SqlScriptExecutorFactory createSqlScriptExecutorFactory(
            final JdbcConnectionFactory jdbcConnectionFactory,
            final CallbackExecutor callbackExecutor,
            final StatementInterceptor statementInterceptor
                                                           );

    /**
     * Initializes the DatabaseExecutionStrategy used by this Database Type.
     *
     * @return The DatabaseExecutionStrategy.
     */
    DatabaseExecutionStrategy createExecutionStrategy(java.sql.Connection connection);

    /**
     * Initializes the ExecutionTemplate used by this Database Type.
     *
     * @return The ExecutionTemplate.
     */
    ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException);

    /**
     * Set the default connection properties for this database. These can be overridden by {@code setConfigConnectionProps}
     * and {@code setOverridingConnectionProps}
     *
     * @param url The JDBC url.
     * @param props The properties to write to.
     * @param classLoader The classLoader to use.
     */
    void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader);

    /**
     * Set any necessary connection properties based on Flyway's configuration. These can be overridden by {@code setOverridingConnectionProps}
     *
     * @param config The Flyway configuration to read properties from
     * @param props The properties to write to.
     * @param classLoader The classLoader to use.
     */
    void setConfigConnectionProps(Configuration config, Properties props, ClassLoader classLoader);

    /**
     * Set any overriding connection properties. These will override anything set by {@code setDefaultConnectionProps}
     * and {@code setConfigConnectionProps} and should only be used if neither of those can satisfy your requirement.
     *
     * @param props The properties to write to.
     */
    void setOverridingConnectionProps(Map<String, String> props);

    /**
     * Shutdown the database that was opened (only applicable to embedded databases that require this).
     *
     * @param url The JDBC url used to create the database.
     * @param driver The driver created for the url.
     */
    void shutdownDatabase(String url, Driver driver);

    /**
     * Detects whether a user is required from configuration. This may not be the case if the driver supports
     * other authentication mechanisms, or supports the user being encoded in the URL
     *
     * @param url The url to check
     * @return true if a username needs to be provided
     */
    boolean detectUserRequiredByUrl(String url);

    /**
     * Detects whether a password is required from configuration. This may not be the case if the driver supports
     * other authentication mechanisms, or supports the password being encoded in the URL
     *
     * @param url The url to check
     * @return true if a password needs to be provided
     */
    boolean detectPasswordRequiredByUrl(String url);

    /**
     * Detects whether or not external authentication is required.
     *
     * @return true if external authentication is required, else false.
     */
    boolean externalAuthPropertiesRequired(String url, String username, String password);

    /**
     * @param url The JDBC url.
     * @param username The username for the connection.
     * @return Authentication properties from database specific locations (e.g. pgpass)
     */
    Properties getExternalAuthProperties(String url, String username);

    /**
     * Carries out any manipulation on the Connection that is required by Flyway's config
     *
     * @param connection The JDBC connection.
     * @param configuration The Flyway configuration.
     */
    Connection alterConnectionAsNeeded(Connection connection, Configuration configuration);

    String instantiateClassExtendedErrorMessage();

    void printMessages();
}