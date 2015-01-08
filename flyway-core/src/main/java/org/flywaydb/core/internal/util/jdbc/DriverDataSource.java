/**
 * Copyright 2010-2015 Axel Fontaine
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
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
     * The ClassLoader to use.
     */
    private final ClassLoader classLoader;

    /**
     * Whether to run in Single Connection mode.
     */
    private boolean singleConnectionMode;

    /**
     * The Single Connection for single connection mode.
     */
    private Connection singleConnection;

    /**
     * Creates a new DriverDataSource.
     *
     * @param classLoader The ClassLoader to use.
     * @param driverClass The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url         The JDBC URL to use for connecting through the Driver. (required)
     * @param user        The JDBC user to use for connecting through the Driver.
     * @param password    The JDBC password to use for connecting through the Driver.
     * @param initSqls    The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, String... initSqls) throws FlywayException {
        if (!StringUtils.hasText(url)) {
            throw new FlywayException("Missing required JDBC URL. Unable to create DataSource!");
        }
        if (!url.toLowerCase().startsWith("jdbc:")) {
            throw new FlywayException("Invalid JDBC URL (should start with jdbc:) : " + url);
        }
        this.classLoader = classLoader;
        this.url = url;

        if (!StringUtils.hasLength(driverClass)) {
            driverClass = detectDriverForUrl(url);
            if (!StringUtils.hasLength(driverClass)) {
                throw new FlywayException("Unable to autodetect JDBC driver for url: " + url);
            }
        }

        try {
            this.driver = ClassUtils.instantiate(driverClass, classLoader);
        } catch (Exception e) {
            String backupDriverClass = getBackupDriverForUrl(url);
            if (backupDriverClass == null) {
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass, e);
            }
            try {
                this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
            } catch (Exception e1) {
                // Only report original exception about primary driver
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass, e);
            }
        }

        this.user = user;
        this.password = password;
        this.initSqls = initSqls;
    }

    /**
     * Retrieves a second choice backup driver for a jdbc url, in case the primary driver is not available.
     *
     * @param url The Jdbc url.
     * @return The Jdbc driver. {@code null} if none.
     */
    private String getBackupDriverForUrl(String url) {
        if (url.startsWith(MYSQL_JDBC_URL_PREFIX)) {
            return MARIADB_JDBC_DRIVER;
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
            singleConnectionMode = true;
            if (new FeatureDetector(classLoader).isAndroidAvailable()) {
                return "org.sqldroid.SQLDroidDriver";
            }
            return "org.sqlite.JDBC";
        }

        if (url.startsWith("jdbc:sqldroid:")) {
            return "org.sqldroid.SQLDroidDriver";
        }

        if (url.startsWith(MYSQL_JDBC_URL_PREFIX)) {
            return "com.mysql.jdbc.Driver";
        }

        if (url.startsWith("jdbc:mariadb:")) {
            return MARIADB_JDBC_DRIVER;
        }

        if (url.startsWith("jdbc:google:")) {
            return "com.google.appengine.api.rdbms.AppEngineDriver";
        }

        if (url.startsWith("jdbc:oracle:")) {
            return "oracle.jdbc.OracleDriver";
        }

        if (url.startsWith("jdbc:phoenix")) {
            return "org.apache.phoenix.jdbc.PhoenixDriver";
        }

        if (url.startsWith("jdbc:postgresql:")) {
            // The format of Redshift JDBC urls is the same as PostgreSQL, and Redshift uses the same JDBC driver
            return "org.postgresql.Driver";
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
    public Connection getConnection() throws SQLException {
        return getConnectionFromDriver(getUser(), getPassword());
    }

    /**
     * This implementation delegates to {@code getConnectionFromDriver},
     * using the given user and password.
     *
     * @see #getConnectionFromDriver(String, String)
     */
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
        if (singleConnectionMode && (singleConnection != null)) {
            return singleConnection;
        }

        Properties props = new Properties();
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
            throw new FlywayException(
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

        if (singleConnectionMode) {
            InvocationHandler suppressCloseHandler = new SuppressCloseHandler(connection);
            singleConnection =
                    (Connection) Proxy.newProxyInstance(classLoader, new Class[]{Connection.class}, suppressCloseHandler);
            return singleConnection;
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

    private static class SuppressCloseHandler implements InvocationHandler {
        private final Connection connection;

        public SuppressCloseHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!"close".equals(method.getName())) {
                return method.invoke(connection, args);
            }

            return null;
        }
    }

    /**
     * Closes this datasource.
     */
    public void close() {
        JdbcUtils.closeConnection(singleConnection);
        singleConnection = null;
    }
}
