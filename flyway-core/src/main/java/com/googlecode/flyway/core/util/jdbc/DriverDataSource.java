/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.util.jdbc;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.util.ClassUtils;
import com.googlecode.flyway.core.util.StringUtils;

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
    /**
     * The JDBC Driver instance to use.
     */
    private Driver driver;

    /**
     * The JDBC URL to use for connecting through the Driver.
     */
    private String url;

    /**
     * The JDBC user to use for connecting through the Driver.
     */
    private String user;

    /**
     * The JDBC password to use for connecting through the Driver.
     */
    private String password;

    /**
     * The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     */
    private String[] initSqls = new String[0];

    /**
     * Creates a new DriverDataSource.
     */
    public DriverDataSource() {
        //Do nothing.
    }

    /**
     * Creates a new DriverDataSource.
     *
     * @param driverClass The name of the JDBC Driver class to use. {@code null} for url-based autodetection.
     * @param url         The JDBC URL to use for connecting through the Driver. (required)
     * @param user        The JDBC user to use for connecting through the Driver.
     * @param password    The JDBC password to use for connecting through the Driver.
     * @param initSqls    The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(String driverClass, String url, String user, String password, String... initSqls) throws FlywayException {
        if (!StringUtils.hasText(url)) {
            throw new FlywayException("Missing required JDBC URL. Unable to create DataSource!");
        }
        if (!url.toLowerCase().startsWith("jdbc:")) {
            throw new FlywayException("Invalid JDBC URL (should start with jdbc:) : " + url);
        }
        this.url = url;

        if (driverClass == null) {
            if (url.startsWith("jdbc:db2:")) {
                driverClass = "com.ibm.db2.jcc.DB2Driver";
            } else if (url.startsWith("jdbc:derby:")) {
                driverClass = "org.apache.derby.jdbc.EmbeddedDriver";
            } else if (url.startsWith("jdbc:h2:")) {
                driverClass = "org.h2.Driver";
            } else if (url.startsWith("jdbc:hsqldb:")) {
                driverClass = "org.hsqldb.jdbcDriver";
            } else if (url.startsWith("jdbc:mysql:")) {
                driverClass = "com.mysql.jdbc.Driver";
            } else if (url.startsWith("jdbc:google:")) {
                driverClass = "com.google.appengine.api.rdbms.AppEngineDriver";
            } else if (url.startsWith("jdbc:oracle:")) {
                driverClass = "oracle.jdbc.OracleDriver";
            } else if (url.startsWith("jdbc:postgresql:")) {
                driverClass = "org.postgresql.Driver";
            } else if (url.startsWith("jdbc:jtds:")) {
                driverClass = "net.sourceforge.jtds.jdbc.Driver";
            } else if (url.startsWith("jdbc:sqlserver:")) {
                driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            } else {
                throw new FlywayException("Unable to autodetect Jdbc driver for url: " + url);
            }
        }

        try {
            this.driver = ClassUtils.instantiate(driverClass);
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate jdbc driver: " + driverClass);
        }

        this.user = user;
        this.password = password;
        this.initSqls = initSqls;
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
        Properties props = new Properties();
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        Connection connection = driver.connect(url, props);

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
