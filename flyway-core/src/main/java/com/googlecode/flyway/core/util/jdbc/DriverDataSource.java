/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.util.ClassUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
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
     * The (optional) sql statement to execute to initialize a connection immediately after obtaining it.
     */
    private String initSql;

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
     */
    public DriverDataSource(Driver driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * @param driver The JDBC Driver instance to use.
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * @param driverClass The class of the JDBC Driver to use.
     *
     * @throws Exception when the driver class can not be instantiated.
     */
    public void setDriverClass(String driverClass) throws Exception{
        this.driver = ClassUtils.instantiate(driverClass);
    }

    /**
     * @return the JDBC Driver instance to use.
     */
    public Driver getDriver() {
        return this.driver;
    }

    /**
     * @param url The JDBC URL to use for connecting through the Driver.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the JDBC URL to use for connecting through the Driver.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param user The JDBC user to use for connecting through the Driver.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the JDBC user to use for connecting through the Driver.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @param password the JDBC password to use for connecting through the Driver.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the JDBC password to use for connecting through the Driver.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @return The (optional) sql statement to execute to initialize a connection immediately after obtaining it.
     */
    public String getInitSql() {
        return initSql;
    }

    /**
     * @param initSql The (optional) sql statement to execute to initialize a connection immediately after obtaining it.
     */
    public void setInitSql(String initSql) {
        this.initSql = initSql;
    }

    /**
     * This implementation delegates to <code>getConnectionFromDriver</code>,
     * using the default user and password of this DataSource.
     *
     * @see #getConnectionFromDriver(String, String)
     * @see #setUser
     * @see #setPassword
     */
    public Connection getConnection() throws SQLException {
        return getConnectionFromDriver(getUser(), getPassword());
    }

    /**
     * This implementation delegates to <code>getConnectionFromDriver</code>,
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
    private Connection getConnectionFromDriver(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        Connection connection = driver.connect(url, props);

        if (initSql != null) {
            connection.createStatement().execute(initSql);
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
