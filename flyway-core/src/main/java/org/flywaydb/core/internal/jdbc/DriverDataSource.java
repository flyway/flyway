/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * YAGNI: The simplest DataSource implementation that works for Flyway.
 */
public class DriverDataSource implements DataSource {
    private Driver driver;
    private final String url;
    private final DatabaseType type;
    private final String user;
    private final String password;
    private final Properties defaultProperties;
    private final Map<String, String> additionalProperties;
    private boolean autoCommit = true;

    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password) throws FlywayException {
        this(classLoader, driverClass, url, user, password, null, new Properties(), new HashMap<>());
    }

    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Configuration configuration) throws FlywayException {
        this(classLoader, driverClass, url, user, password, configuration, new Properties(), new HashMap<>());
    }

    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Map<String, String> additionalProperties) throws FlywayException {
        this(classLoader, driverClass, url, user, password, null, new Properties(), additionalProperties);
    }

    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Configuration configuration,
                            Map<String, String> additionalProperties) throws FlywayException {
        this(classLoader, driverClass, url, user, password, configuration, new Properties(), additionalProperties);
    }

    /**
     * Creates a new DriverDataSource.
     *
     * @param classLoader           The ClassLoader to use.
     * @param driverClass           The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url                   The JDBC URL to use for connecting through the Driver. (required)
     * @param user                  The JDBC user to use for connecting through the Driver.
     * @param password              The JDBC password to use for connecting through the Driver.
     * @param configuration         The Flyway configuration
     * @param defaultProperties     Default values of properties to pass to the connection (can be overridden by {@code additionalProperties})
     * @param additionalProperties  The properties to pass to the connection.
     * @throws FlywayException      when the datasource could not be created.
     */
    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Configuration configuration, Properties defaultProperties,
                            Map<String, String> additionalProperties) throws FlywayException {
        this.url = detectFallbackUrl(url);

        this.type = DatabaseTypeRegister.getDatabaseTypeForUrl(url);

        if (!StringUtils.hasLength(driverClass)) {
            if (type == null) {
                throw new FlywayException("Unable to autodetect JDBC driver for url: " + DatabaseTypeRegister.redactJdbcUrl(url));
            }

            driverClass =  type.getDriverClass(url, classLoader);
        }

        if (additionalProperties != null) {
            this.additionalProperties = additionalProperties;
        } else {
            this.additionalProperties = new HashMap<>();
        }
        this.defaultProperties = new Properties(defaultProperties);
        type.setDefaultConnectionProps(url, defaultProperties, classLoader);
        type.setConfigConnectionProps(configuration, defaultProperties, classLoader);
        type.setOverridingConnectionProps(this.additionalProperties);

        try {
            this.driver = ClassUtils.instantiate(driverClass, classLoader);
        } catch (FlywayException e) {
            String backupDriverClass = type.getBackupDriverClass(url, classLoader);
            if (backupDriverClass == null) {
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass
                        + " => Check whether the jar file is present", e,
                        ErrorCode.JDBC_DRIVER);
            }
            try {
                this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
            } catch (Exception e1) {
                // Only report original exception about primary driver
                throw new FlywayException(
                        "Unable to instantiate JDBC driver: " + driverClass + " or backup driver: " + backupDriverClass + " => Check whether the jar file is present", e,
                        ErrorCode.JDBC_DRIVER);
            }
        }

        this.user = detectFallbackUser(user);
        this.password = detectFallbackPassword(password);

        if (type.externalAuthPropertiesRequired(url, user, password)) {
            defaultProperties.putAll(type.getExternalAuthProperties(url, user));
        }
    }

    /**
     * Detects a fallback url in case this one is missing.
     *
     * @param url The url to check.
     * @return The url to use.
     */
    private String detectFallbackUrl(String url) {
        if (!StringUtils.hasText(url)) {
            // Attempt fallback to the automatically provided Boxfuse database URL (https://boxfuse.com/docs/databases#envvars)
            String boxfuseDatabaseUrl = System.getenv("BOXFUSE_DATABASE_URL");
            if (StringUtils.hasText(boxfuseDatabaseUrl)) {
                return boxfuseDatabaseUrl;
            }

            throw new FlywayException("Missing required JDBC URL. Unable to create DataSource!");
        }

        return url;
    }

    /**
     * Detects a fallback user in case this one is missing.
     *
     * @param user The user to check.
     * @return The user to use.
     */
    private String detectFallbackUser(String user) {
        if (!StringUtils.hasText(user)) {
            // Attempt fallback to the automatically provided Boxfuse database user (https://boxfuse.com/docs/databases#envvars)
            String boxfuseDatabaseUser = System.getenv("BOXFUSE_DATABASE_USER");
            if (StringUtils.hasText(boxfuseDatabaseUser)) {
                return boxfuseDatabaseUser;
            }
        }
        return user;
    }

    /**
     * Detects a fallback password in case this one is missing.
     *
     * @param password The password to check.
     * @return The password to use.
     */
    private String detectFallbackPassword(String password) {
        if (!StringUtils.hasText(password)) {
            // Attempt fallback to the automatically provided Boxfuse database password (https://boxfuse.com/docs/databases#envvars)
            String boxfuseDatabasePassword = System.getenv("BOXFUSE_DATABASE_PASSWORD");
            if (StringUtils.hasText(boxfuseDatabasePassword)) {
                return boxfuseDatabasePassword;
            }
        }
        return password;
    }

    /**
     * @return the JDBC Driver instance to use.
     */
    public Driver getDriver() {
        return this.driver;
    }

    /**
     * @return the JDBC URL to use for connecting through the Driver.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return the JDBC user to use for connecting through the Driver.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @return the JDBC password to use for connecting through the Driver.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @return The additional properties to pass to a JDBC connection.
     */
    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    /**
     * This implementation delegates to {@code getConnectionFromDriver},
     * using the default user and password of this DataSource.
     *
     * @see #getConnectionFromDriver(String, String)
     */
    @Override
    public Connection getConnection() throws SQLException {
        return getConnectionFromDriver(getUser(), getPassword());
    }

    /**
     * This implementation delegates to {@code getConnectionFromDriver},
     * using the given user and password.
     *
     * @see #getConnectionFromDriver(String, String)
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnectionFromDriver(username, password);
    }

    /**
     * Build properties for the Driver, including the given user and password (if any),
     * and obtain a corresponding Connection.
     *
     * @param username the name of the user
     * @param password the password to use
     * @return the obtained Connection
     * @throws SQLException in case of failure
     * @see java.sql.Driver#connect(String, java.util.Properties)
     */
    protected Connection getConnectionFromDriver(String username, String password) throws SQLException {
        Properties properties = new Properties(this.defaultProperties);

        if (username != null) {
            properties.setProperty("user", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }

        properties.putAll(additionalProperties);

        Connection connection = driver.connect(url, properties);
        if (connection == null) {
            throw new FlywayException("Unable to connect to " + DatabaseTypeRegister.redactJdbcUrl(url));
        }
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    /**
     * @return Whether connection should have auto commit activated or not. Default: {@code true}
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * @param autoCommit Whether connection should have auto commit activated or not. Default: {@code true}
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void shutdownDatabase() {
        type.shutdownDatabase(url, driver);
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public void setLoginTimeout(int timeout) {
        unsupportedMethod("setLoginTimeout");
    }

    @Override
    public PrintWriter getLogWriter() {
        unsupportedMethod("getLogWriter");
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter pw) {
        unsupportedMethod("setLogWriter");
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        unsupportedMethod("unwrap");
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return DataSource.class.equals(iface);
    }

    @Override
    public Logger getParentLogger() {
        unsupportedMethod("getParentLogger");
        return null;
    }

    private void unsupportedMethod(String methodName) {
        throw new UnsupportedOperationException(methodName);
    }
}