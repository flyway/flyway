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
package org.flywaydb.core.internal.database.base;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DefaultExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.jdbc.*;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.DefaultSqlScriptExecutor;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.sql.Connection;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.flywaydb.core.internal.sqlscript.SqlScriptMetadata.getMetadataResource;

@CustomLog
public abstract class BaseDatabaseType implements DatabaseType {
    // Don't grab semicolons and ampersands - they have special meaning in URLs
    private static final Pattern defaultJdbcCredentialsPattern = Pattern.compile("password=([^;&]*).*", Pattern.CASE_INSENSITIVE);

    /**
     * This is useful for databases that allow setting this in order to easily correlate individual application with
     * database connections.
     */
    protected static final String APPLICATION_NAME = "Flyway by Redgate";

    /**
     * @return The human-readable name for this database.
     */
    public abstract String getName();

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return The JDBC type used to represent {@code null} in prepared statements.
     */
    public abstract int getNullType();

    @Override
    public boolean supportsReadOnlyTransactions() {
        return true;
    }

    /**
     * Whether this database type should handle the given JDBC url.
     */
    public abstract boolean handlesJDBCUrl(String url);

    /**
     * A regex that identifies credentials in the JDBC URL, where they conform to a pattern specific to this database.
     * The first captured group should represent the password text, so that it can be redacted if necessary.
     *
     * @return The URL regex.
     */
    public Pattern getJDBCCredentialsPattern() {
        return defaultJdbcCredentialsPattern;
    }

    /**
     * Gets a regex that identifies credentials in the JDBC URL, where they conform to the default URL pattern.
     * The first captured group represents the password text.
     */
    public static Pattern getDefaultJDBCCredentialsPattern() {
        return defaultJdbcCredentialsPattern;
    }

    /**
     * @return The full driver class name to be instantiated to handle this url.
     */
    public abstract String getDriverClass(String url, ClassLoader classLoader);

    /**
     * Retrieves a second choice backup driver for a JDBC url, in case the one returned by {@code getDriverClass} is
     * not available.
     *
     * @return The JDBC driver class name, {@code null} if none.
     */
    public String getBackupDriverClass(String url, ClassLoader classLoader) {
        return null;
    }

    /**
     * This allows more fine-grained control over which DatabaseType handles which connection.
     * Flyway will use the first DatabaseType that returns true for this method.
     */
    public abstract boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection);

    public Database createDatabase(Configuration configuration, boolean printInfo, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        String databaseProductName = jdbcConnectionFactory.getProductName();
        if (printInfo) {
            LOG.info("Database: " + jdbcConnectionFactory.getJdbcUrl() + " (" + databaseProductName + ")");
            LOG.debug("Driver  : " + jdbcConnectionFactory.getDriverInfo());
        }

        Database database = createDatabase(configuration, jdbcConnectionFactory, statementInterceptor);

        return database;
    }

    public abstract Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor);

    public abstract Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext);

    public SqlScriptFactory createSqlScriptFactory(final Configuration configuration, final ParsingContext parsingContext) {
        return (resource, mixed, resourceProvider) -> new ParserSqlScript(createParser(configuration, resourceProvider, parsingContext),
                                                                          resource, getMetadataResource(resourceProvider, resource), mixed);
    }

    public SqlScriptExecutorFactory createSqlScriptExecutorFactory(final JdbcConnectionFactory jdbcConnectionFactory,
                                                                   final CallbackExecutor callbackExecutor,
                                                                   final StatementInterceptor statementInterceptor) {
        boolean supportsBatch = false;




        final boolean finalSupportsBatch = supportsBatch;
        final DatabaseType thisRef = this;

        return (connection, undo, batch, outputQueryResults) -> new DefaultSqlScriptExecutor(new JdbcTemplate(connection, thisRef),
                                                                                             callbackExecutor, undo, finalSupportsBatch && batch, outputQueryResults, statementInterceptor);
    }

    public DatabaseExecutionStrategy createExecutionStrategy(java.sql.Connection connection) {
        return new DefaultExecutionStrategy();
    }

    public ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        return new TransactionalExecutionTemplate(connection, rollbackOnException);
    }

    /**
     * Retrieves the version string for a connection as described by SELECT VERSION(), which may differ from the
     * connection metadata.
     */
    public static String getSelectVersionOutput(Connection connection) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String result = null;

        try {
            statement = connection.prepareStatement("SELECT version()");
            resultSet = statement.executeQuery();
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
     * Set the default connection properties for this database. These can be overridden by
     * {@code setConfigConnectionProps} and {@code setOverridingConnectionProps}.
     *
     * @param url The JDBC url.
     * @param props The properties to write to.
     * @param classLoader The classLoader to use.
     */
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {}

    /**
     * Set any necessary connection properties based on Flyway's configuration. These can be overridden by
     * {@code setOverridingConnectionProps}.
     *
     * @param config The Flyway configuration to read properties from.
     * @param props The properties to write to.
     * @param classLoader The classLoader to use.
     */
    public void setConfigConnectionProps(Configuration config, Properties props, ClassLoader classLoader) {}

    /**
     * These will override anything set by {@code setDefaultConnectionProps} and {@code setConfigConnectionProps} and
     * should only be used if neither of those can satisfy your requirement.
     *
     * @param props The properties to write to.
     */
    public void setOverridingConnectionProps(Map<String, String> props) {}

    /**
     * Only applicable to embedded databases that require this.
     */
    public void shutdownDatabase(String url, Driver driver) {}

    /**
     * Detects whether a user is required from configuration. This may not be the case if the driver supports
     * other authentication mechanisms, or supports the user being encoded in the URL.
     */
    public boolean detectUserRequiredByUrl(String url) {
        return true;
    }

    /**
     * Detects whether a password is required from configuration. This may not be the case if the driver supports
     * other authentication mechanisms, or supports the password being encoded in the URL.
     */
    public boolean detectPasswordRequiredByUrl(String url) {
        return true;
    }

    public boolean externalAuthPropertiesRequired(String url, String username, String password) {
        return false;
    }

    public Properties getExternalAuthProperties(String url, String username) {
        return new Properties();
    }

    public Connection alterConnectionAsNeeded(Connection connection, Configuration configuration) {
        return connection;
    }

    public String instantiateClassExtendedErrorMessage() {
        return "";
    }

    public void printMessages() {}
}