/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.util.ClassUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

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
     * @param driver The JDBC Driver instance to use.
     * @param url The JDBC URL to use for connecting through the Driver.
     * @param user The JDBC user to use for connecting through the Driver.
     * @param password The JDBC password to use for connecting through the Driver.
     * @param initSqls The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     *
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(Driver driver, String url, String user, String password, String... initSqls) throws FlywayException {
        configure(driver, url, user, password, initSqls);
    }

    /**
     * Creates a new DriverDataSource.
     *
     * @param driverClass The name of the JDBC Driver class to use.
     * @param url The JDBC URL to use for connecting through the Driver.
     * @param user The JDBC user to use for connecting through the Driver.
     * @param password The JDBC password to use for connecting through the Driver.
     * @param initSqls The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     *
     * @throws FlywayException when the datasource could not be created.
     */
    public DriverDataSource(String driverClass, String url, String user, String password, String... initSqls) throws FlywayException {
        Driver driver;
        try {
            driver = ClassUtils.instantiate(driverClass);
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate jdbc driver: " +  driverClass);
        }
        configure(driver, url, user, password, initSqls);
    }

    /**
     * Configures this DriverDataSource.
     *
     * @param driver The JDBC Driver instance to use.
     * @param url The JDBC URL to use for connecting through the Driver.
     * @param user The JDBC user to use for connecting through the Driver.
     * @param password The JDBC password to use for connecting through the Driver.
     * @param initSqls The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     *
     * @throws FlywayException when the datasource could not be configured.
     */
    private void configure(Driver driver, String url, String user, String password, String... initSqls) throws FlywayException {
        this.driver = driver;

        if (!url.toLowerCase().startsWith("jdbc:")) {
            throw new FlywayException("Invalid jdbc url (should start with jdbc:) : " + url);
        }
        this.url = url;

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

        JdbcTemplate jdbcTemplate = new JdbcTemplate(connection);
        for (String initSql : initSqls) {
            jdbcTemplate.executeStatement(initSql);
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
}
