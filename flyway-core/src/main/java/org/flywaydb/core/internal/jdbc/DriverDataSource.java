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
package org.flywaydb.core.internal.jdbc;

import java.util.List;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.api.CoreErrorCode;
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
@Getter
@CustomLog
public class DriverDataSource implements DataSource {
    /**
     * @return the JDBC Driver instance to use.
     */
    private Driver driver;
    /**
     * @return the JDBC URL to use for connecting through the Driver.
     */
    private final String url;
    @Getter(AccessLevel.NONE)
    private DatabaseType type;
    /**
     * @return the JDBC user to use for connecting through the Driver.
     */
    private final String user;
    /**
     * @return the JDBC password to use for connecting through the Driver.
     */
    private final String password;
    @Getter(AccessLevel.NONE)
    private final Properties defaultProperties;
    /**
     * @return The additional properties to pass to a JDBC connection.
     */
    private final Map<String, String> additionalProperties;
    /**
     * @param autoCommit Whether connection should have auto commit activated or not. Default: {@code true}
     * @return Whether connection should have auto commit activated or not. Default: {@code true}
     */
    @Setter
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
     * @param classLoader The ClassLoader to use.
     * @param driverClass The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url The JDBC URL to use for connecting through the Driver. (required)
     * @param user The JDBC user to use for connecting through the Driver.
     * @param password The JDBC password to use for connecting through the Driver.
     * @param configuration The Flyway configuration
     * @param defaultProperties Default values of properties to pass to the connection (can be overridden by {@code additionalProperties})
     * @param additionalProperties The properties to pass to the connection.
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Configuration configuration, Properties defaultProperties,
                            Map<String, String> additionalProperties) throws FlywayException {
        this.url = detectFallbackUrl(url);

        List<DatabaseType> typesAcceptingUrl = DatabaseTypeRegister.getDatabaseTypesForUrl(url, configuration);

        for (DatabaseType type: typesAcceptingUrl) {
            String mainDriverClass = StringUtils.hasLength(driverClass) ? driverClass : type.getDriverClass(url, classLoader);

            try {
                this.driver = ClassUtils.instantiate(mainDriverClass, classLoader);
            } catch (FlywayException e) {
                String extendedError = type.instantiateClassExtendedErrorMessage();

                /* If the user-provided driverClass failed, no need to check backup driverClass or any other candidates in the queue */
                if (StringUtils.hasLength(driverClass)) {
                    throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass
                                                  + " => Check whether the jar file is present"
                                                  + extendedError, e,
                                              CoreErrorCode.JDBC_DRIVER);
                }

                String backupDriverClass = type.getBackupDriverClass(url, classLoader);

                if (backupDriverClass == null) {
                    if (StringUtils.hasText(extendedError)) {
                        extendedError = System.lineSeparator() + extendedError;
                    }

                    LOG.debug("Unable to instantiate JDBC driver: " + mainDriverClass + " => Check whether the jar file is present." + extendedError);
                    continue;
                }

                try {
                    this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
                } catch (Exception e1) {
                    LOG.debug("Unable to instantiate JDBC driver: " + mainDriverClass + " or backup driver: " + backupDriverClass + " => Check whether the jar file is present");
                    continue;
                }
            }

            this.type = type;
            break;
        }

        if (this.type == null) {
            throw new FlywayException("No database found to handle " + DatabaseTypeRegister.redactJdbcUrl(url));
        }

        if (additionalProperties != null) {
            this.additionalProperties = additionalProperties;
        } else {
            this.additionalProperties = new HashMap<>();
        }
        this.defaultProperties = new Properties(defaultProperties);
        type.setDefaultConnectionProps(url, this.defaultProperties, classLoader);
        type.setConfigConnectionProps(configuration, this.defaultProperties, classLoader);
        type.setOverridingConnectionProps(this.additionalProperties);
        this.user = detectFallbackUser(user);
        this.password = detectFallbackPassword(password);

        if (type.externalAuthPropertiesRequired(url, user, password)) {
            this.defaultProperties.putAll(type.getExternalAuthProperties(url, user));
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
        properties.putAll(additionalProperties);

        if (username != null) {
            properties.setProperty("user", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }

        Connection connection = driver.connect(url, properties);
        if (connection == null) {
            throw new FlywayException("Unable to connect to " + DatabaseTypeRegister.redactJdbcUrl(url));
        }
        connection.setAutoCommit(autoCommit);
        return connection;
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
