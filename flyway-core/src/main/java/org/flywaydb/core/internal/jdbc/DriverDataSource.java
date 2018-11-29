/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * YAGNI: The simplest DataSource implementation that works for Flyway.
 */
public class DriverDataSource implements DataSource {
    private static final Log LOG = LogFactory.getLog(DriverDataSource.class);

    private static final String DB2_JDBC_URL_PREFIX = "jdbc:db2:";
    private static final String DERBY_CLIENT_JDBC_URL_PREFIX = "jdbc:derby://";
    private static final String DERBY_EMBEDDED_JDBC_URL_PREFIX = "jdbc:derby:";
    private static final String MARIADB_JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String MARIADB_JDBC_URL_PREFIX = "jdbc:mariadb:";
    private static final String MYSQL_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_LEGACY_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String MYSQL_JDBC_URL_PREFIX = "jdbc:mysql:";
    private static final String ORACLE_JDBC_URL_PREFIX = "jdbc:oracle:";
    private static final String POSTGRESQL_JDBC_URL_PREFIX = "jdbc:postgresql:";
    private static final String REDSHIFT_JDBC_URL_PREFIX = "jdbc:redshift:";
    private static final String REDSHIFT_JDBC41_DRIVER = "com.amazon.redshift.jdbc41.Driver";
    private static final String SAPHANA_JDBC_URL_PREFIX = "jdbc:sap:";
    private static final String SQLDROID_DRIVER = "org.sqldroid.SQLDroidDriver";
    private static final String SQLSERVER_JDBC_URL_PREFIX = "jdbc:sqlserver:";
    private static final String SYBASE_JDBC_URL_PREFIX = "jdbc:sybase:";

    /**
     * The name of the application that created the connection. This is useful for databases that allow setting this
     * in order to easily correlate individual application with database connections.
     */
    private static final String APPLICATION_NAME = "Flyway by Boxfuse";

    /**
     * The JDBC Driver instance to use.
     */
    private Driver driver;

    /**
     * The JDBC URL to use for connecting through the Driver.
     */
    private final String url;

    /**
     * The JDBC user to use for connecting through the Driver.
     */
    private final String user;

    /**
     * The JDBC password to use for connecting through the Driver.
     */
    private final String password;

    /**
     * The properties to be passed to a new connection.
     */
    private final Properties defaultProps;

    /**
     * The ClassLoader to use.
     */
    private final ClassLoader classLoader;

    /**
     * Whether connection should have auto commit activated or not. Default: {@code true}
     */
    private boolean autoCommit = true;

    /**
     * Creates a new DriverDataSource.
     *
     * @param classLoader The ClassLoader to use.
     * @param driverClass The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url         The JDBC URL to use for connecting through the Driver. (required)
     * @param user        The JDBC user to use for connecting through the Driver.
     * @param password    The JDBC password to use for connecting through the Driver.
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password) throws FlywayException {
        this(classLoader, driverClass, url, user, password, new Properties());
    }

    /**
     * Creates a new DriverDataSource.
     *
     * @param classLoader The ClassLoader to use.
     * @param driverClass The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url         The JDBC URL to use for connecting through the Driver. (required)
     * @param user        The JDBC user to use for connecting through the Driver.
     * @param password    The JDBC password to use for connecting through the Driver.
     * @param props       The properties to pass to the connection.
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password,
                            Properties props) throws FlywayException {
        this.classLoader = classLoader;
        this.url = detectFallbackUrl(url);

        if (!StringUtils.hasLength(driverClass)) {
            driverClass = detectDriverForUrl(url);
            if (!StringUtils.hasLength(driverClass)) {
                throw new FlywayException("Unable to autodetect JDBC driver for url: " + url);
            }
        }

        this.defaultProps = new Properties(props);
        this.defaultProps.putAll(detectPropsForUrl(url));

        try {
            this.driver = ClassUtils.instantiate(driverClass, classLoader);
        } catch (FlywayException e) {
            String backupDriverClass = detectBackupDriverForUrl(url);
            if (backupDriverClass == null) {
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass
                        + " => Check whether the jar file is present", e);
            }
            try {
                this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
            } catch (Exception e1) {
                // Only report original exception about primary driver
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass + " => Check whether the jar file is present", e);
            }
        }

        this.user = detectFallbackUser(user);
        this.password = detectFallbackPassword(password);
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

        if (!url.toLowerCase().startsWith("jdbc:")) {
            throw new FlywayException("Invalid JDBC URL (should start with jdbc:) : " + url);
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
     * Detect the default connection properties for this url.
     *
     * @param url The Jdbc url.
     * @return The properties.
     */
    private Properties detectPropsForUrl(String url) {
        Properties result = new Properties();

        if (url.startsWith(ORACLE_JDBC_URL_PREFIX)) {
            String osUser = System.getProperty("user.name");
            result.put("v$session.osuser", osUser.substring(0, Math.min(osUser.length(), 30)));
            result.put("v$session.program", APPLICATION_NAME);
            result.put("oracle.net.keepAlive", "true");
        } else if (url.startsWith(SQLSERVER_JDBC_URL_PREFIX)) {
            result.put("applicationName", APPLICATION_NAME);
        } else if (url.startsWith(POSTGRESQL_JDBC_URL_PREFIX)) {
            result.put("ApplicationName", APPLICATION_NAME);
        } else if (url.startsWith(MYSQL_JDBC_URL_PREFIX) || url.startsWith(MARIADB_JDBC_URL_PREFIX)) {
            result.put("connectionAttributes", "program_name:" + APPLICATION_NAME);
        } else if (url.startsWith(DB2_JDBC_URL_PREFIX)) {
            result.put("clientProgramName", APPLICATION_NAME);
            result.put("retrieveMessagesFromServerOnGetMessage", "true");
        } else if (url.startsWith(SYBASE_JDBC_URL_PREFIX)) {
            result.put("APPLICATIONNAME", APPLICATION_NAME);
        } else if (url.startsWith(SAPHANA_JDBC_URL_PREFIX)) {
            result.put("SESSIONVARIABLE:APPLICATION", APPLICATION_NAME);
        }

        return result;
    }

    /**
     * Retrieves a second choice backup driver for a JDBC URL, in case the primary driver is not available.
     *
     * @param url The JDBC url.
     * @return The JDBC driver. {@code null} if none.
     */
    private String detectBackupDriverForUrl(String url) {
        if (url.startsWith(MYSQL_JDBC_URL_PREFIX) && ClassUtils.isPresent(MYSQL_LEGACY_JDBC_DRIVER, classLoader)) {
            return MYSQL_LEGACY_JDBC_DRIVER;
        }

        if (url.startsWith(MYSQL_JDBC_URL_PREFIX) && ClassUtils.isPresent(MARIADB_JDBC_DRIVER, classLoader)) {
            LOG.warn("You are attempting to connect to a MySQL database using the MariaDB driver." +
                    " This is known to cause issues." +
                    " An upgrade to Oracle's MySQL JDBC driver is highly recommended.");
            return MARIADB_JDBC_DRIVER;
        }

        if (url.startsWith(REDSHIFT_JDBC_URL_PREFIX)) {
            if (ClassUtils.isPresent(REDSHIFT_JDBC41_DRIVER, classLoader)) {
                return REDSHIFT_JDBC41_DRIVER;
            }
            return "com.amazon.redshift.jdbc4.Driver";
        }

        return null;
    }

    /**
     * Detects the correct Jdbc driver for this Jdbc url.
     *
     * @param url The Jdbc url.
     * @return The Jdbc driver.
     */
    private String detectDriverForUrl(String url) {
        if (url.startsWith("jdbc:tc:")) {
            return "org.testcontainers.jdbc.ContainerDatabaseDriver";
        }

        if (url.startsWith(DB2_JDBC_URL_PREFIX)) {
            return "com.ibm.db2.jcc.DB2Driver";
        }

        if (url.startsWith(DERBY_CLIENT_JDBC_URL_PREFIX)) {
            return "org.apache.derby.jdbc.ClientDriver";
        }

        if (url.startsWith(DERBY_EMBEDDED_JDBC_URL_PREFIX)) {
            return "org.apache.derby.jdbc.EmbeddedDriver";
        }

        if (url.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        }

        if (url.startsWith("jdbc:hsqldb:")) {
            return "org.hsqldb.jdbcDriver";
        }

        if (url.startsWith("jdbc:sqlite:")) {
            if (new FeatureDetector(classLoader).isAndroidAvailable()) {
                return SQLDROID_DRIVER;
            }
            return "org.sqlite.JDBC";
        }

        if (url.startsWith("jdbc:sqldroid:")) {
            return SQLDROID_DRIVER;
        }

        if (url.startsWith(MYSQL_JDBC_URL_PREFIX)) {
            return MYSQL_JDBC_DRIVER;
        }

        if (url.startsWith(MARIADB_JDBC_URL_PREFIX)) {
            return MARIADB_JDBC_DRIVER;
        }

        if (url.startsWith("jdbc:google:")) {
            return "com.mysql.jdbc.GoogleDriver";
        }

        if (url.startsWith(ORACLE_JDBC_URL_PREFIX)) {
            return "oracle.jdbc.OracleDriver";
        }

        if (url.startsWith(POSTGRESQL_JDBC_URL_PREFIX)) {
            return "org.postgresql.Driver";
        }

        if (url.startsWith(REDSHIFT_JDBC_URL_PREFIX)) {
            return "com.amazon.redshift.jdbc42.Driver";
        }

        if (url.startsWith("jdbc:jtds:")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }

        if (url.startsWith(SYBASE_JDBC_URL_PREFIX)) {
            return "com.sybase.jdbc4.jdbc.SybDriver";
        }

        if (url.startsWith(SQLSERVER_JDBC_URL_PREFIX)) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }

        if (url.startsWith(SAPHANA_JDBC_URL_PREFIX)) {
            return "com.sap.db.jdbc.Driver";
        }

        if (url.startsWith("jdbc:informix-sqli:")) {
            return "com.informix.jdbc.IfxDriver";
        }

        return null;
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
        Properties props = new Properties(this.defaultProps);
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }

        Connection connection = driver.connect(url, props);
        if (connection == null) {
            throw new FlywayException("Unable to connect to " + url);
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

    public int getLoginTimeout() {
        return 0;
    }

    public void setLoginTimeout(int timeout) {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    public void setLogWriter(PrintWriter pw) {
        throw new UnsupportedOperationException("setLogWriter");
    }

    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException("unwrap");
    }

    public boolean isWrapperFor(Class<?> iface) {
        return DataSource.class.equals(iface);
    }

    public Logger getParentLogger() {
        throw new UnsupportedOperationException("getParentLogger");
    }

    /**
     * Shutdown the database that was opened (only applicable to embedded databases that require this).
     */
    public void shutdownDatabase() {
        if (url.startsWith(DERBY_EMBEDDED_JDBC_URL_PREFIX) && !url.startsWith(DERBY_CLIENT_JDBC_URL_PREFIX)) {
            try {
                int i = url.indexOf(";");
                String shutdownUrl = (i < 0 ? url : url.substring(0, i)) + ";shutdown=true";

                driver.connect(shutdownUrl, new Properties());
            } catch (SQLException e) {
                LOG.debug("Expected error on Derby Embedded Database shutdown: " + e.getMessage());
            }
        }
    }
}