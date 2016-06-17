package org.flywaydb.core.internal.util.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * SingleConnectionDataSourceProxy.
 */
public class SingleConnectionDataSourceProxy implements DataSource {
    
    private final DataSource delegate;
    private Connection connection;

    public SingleConnectionDataSourceProxy(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = delegate.getConnection();
        }
        return connection;
    }

    @Override
    public synchronized Connection getConnection(String username, String password) throws SQLException {
        if (connection == null) {
            connection = delegate.getConnection(username, password);
        }
        return connection;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("getParentLogger");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
    
}
