/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * YAGNI: The simplest DataSource implementation that works for Flyway.
 */
public class DriverDataSource implements DataSource {
    private static final String MARIADB_JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String MYSQL_JDBC_URL_PREFIX = "jdbc:mysql:";
    private static final String ORACLE_JDBC_URL_PREFIX = "jdbc:oracle:";
    private static final String MYSQL_5_JDBC_DRIVER = "com.mysql.jdbc.Driver";

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
     * The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     */
    private final String[] initSqls;

    /**
     * The properties to be passed to a new connection.
     */
    private final Properties defaultProps;

    /**
     * The ClassLoader to use.
     */
    private final ClassLoader classLoader;

    /**
     * Creates a new DriverDataSource.
     *
     * @param classLoader The ClassLoader to use.
     * @param driverClass The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url         The JDBC URL to use for connecting through the Driver. (required)
     * @param user        The JDBC user to use for connecting through the Driver.
     * @param password    The JDBC password to use for connecting through the Driver.
     * @param props       The properties to pass to the connection.
     * @param initSqls    The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Properties props, String... initSqls) throws FlywayException {
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
        } catch (Exception e) {
            String backupDriverClass = detectBackupDriverForUrl(url);
            if (backupDriverClass == null) {
                throw new FlywayException("Unable to instantiate JDBC driver " + driverClass + " : " + e.getMessage(), e);
            }
            try {
                this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
            } catch (Exception e1) {
                // Only report original exception about primary driver
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass + " : " + e.getMessage(), e);
            }
        }

        this.user = detectFallbackUser(user);
        this.password = detectFallbackPassword(password);

        if (initSqls == null) {
            initSqls = new String[0];
        }
        this.initSqls = initSqls;
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
            result.put("v$session.program", "Flyway by Boxfuse");
        }

        return result;
    }

    /**
     * Retrieves a second choice backup driver for a jdbc url, in case the primary driver is not available.
     *
     * @param url The Jdbc url.
     * @return The Jdbc driver. {@code null} if none.
     */
    private String detectBackupDriverForUrl(String url) {
        if (url.startsWith(MYSQL_JDBC_URL_PREFIX)) {
            if (ClassUtils.isPresent(MYSQL_5_JDBC_DRIVER, classLoader)) {
                return MYSQL_5_JDBC_DRIVER;
            }

            return MARIADB_JDBC_DRIVER;
        }

        if (url.startsWith("jdbc:redshift:")) {
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
        if (url.startsWith("jdbc:db2:")) {
            return "com.ibm.db2.jcc.DB2Driver";
        }

        if (url.startsWith("jdbc:derby://")) {
            return "org.apache.derby.jdbc.ClientDriver";
        }

        if (url.startsWith("jdbc:derby:")) {
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
                return "org.sqldroid.SQLDroidDriver";
            }
            return "org.sqlite.JDBC";
        }

        if (url.startsWith("jdbc:sqldroid:")) {
            return "org.sqldroid.SQLDroidDriver";
        }

        if (url.startsWith(MYSQL_JDBC_URL_PREFIX)) {
            return "com.mysql.cj.jdbc.Driver";
        }

        if (url.startsWith("jdbc:mariadb:")) {
            return MARIADB_JDBC_DRIVER;
        }

        if (url.startsWith("jdbc:google:")) {
            return "com.mysql.jdbc.GoogleDriver";
        }

        if (url.startsWith(ORACLE_JDBC_URL_PREFIX)) {
            return "oracle.jdbc.OracleDriver";
        }

        if (url.startsWith("jdbc:edb:")) {
            return "com.edb.Driver";
        }

        if (url.startsWith("jdbc:phoenix")) {
            return "org.apache.phoenix.jdbc.PhoenixDriver";
        }

        if (url.startsWith("jdbc:postgresql:")) {
            // The old format of Redshift JDBC urls is the same as PostgreSQL, and Redshift can still use the same JDBC driver:
            return "org.postgresql.Driver";
        }

        if (url.startsWith("jdbc:redshift:")) {
            // The new format of Redshift JDBC urls, using the new Redshift-specific JDBC driver:
            return "com.amazon.redshift.jdbc41.Driver";
        }

        if (url.startsWith("jdbc:jtds:")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }

        if (url.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }

        if (url.startsWith("jdbc:vertica:")) {
            return "com.vertica.jdbc.Driver";
        }

        if (url.startsWith("jdbc:sap:")) {
            return "com.sap.db.jdbc.Driver";
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
     * @return The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     */
    public String[] getInitSqls() {
        return initSqls;
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
        Connection connection;
        try {
            connection = driver.connect(url, props);
        } catch (SQLException e) {
            throw new FlywaySqlException(
                    "Unable to obtain Jdbc connection from DataSource (" + url + ") for user '" + user + "': " + e.getMessage(), e);
        }

        for (String initSql : initSqls) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                statement.execute(initSql);
            } finally {
                JdbcUtils.closeStatement(statement);
            }
        }

        return connection;
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    public void setLogWriter(PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("unwrap");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return DataSource.class.equals(iface);
    }

    public Logger getParentLogger() {
        throw new UnsupportedOperationException("getParentLogger");
    }
}
