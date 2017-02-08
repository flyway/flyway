package org.flywaydb.core.internal.dbsupport.hive.driver;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Proxy of HiveDriver which provided an implementation for some methods which throw exceptions
 */
public class HiveProxyDriver implements Driver {

    private final Driver delegate;

    public HiveProxyDriver() {
        try {
            delegate = (Driver)Class.forName("org.apache.hive.jdbc.HiveDriver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot initialize HiveDriver", e);
        }
    }

    public Connection connect(String url, Properties info) throws SQLException {
        return new HiveProxyConnection(delegate.connect(url, info));
    }

    public boolean acceptsURL(String url) throws SQLException {
        return delegate.acceptsURL(url);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }


}
