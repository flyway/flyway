package org.flywaydb.core.internal.dbsupport.hive.driver;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class HiveProxyConnection implements Connection {


    private final Connection delegate;

    public HiveProxyConnection(Connection delegate) {
        this.delegate = delegate;
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        // No action: not applicable for Hive
    }

    public boolean getAutoCommit() throws SQLException {
        return true;
    }

    public void commit() throws SQLException {
        // No action: not applicable for Hive
    }

    public void rollback() throws SQLException {
        // No action: not applicable for Hive
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return new HiveProxyMetadata(delegate.getMetaData());
    }




    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public Statement createStatement() throws SQLException {
        return delegate.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return delegate.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return delegate.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }


    public void close() throws SQLException {
        delegate.close();
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }


    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return delegate.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return delegate.prepareStatement(sql, columnNames);
    }

    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }

    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    private class MyConnection implements Connection {
        @Override
        public Statement createStatement() throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return null;
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return null;
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {

        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return false;
        }

        @Override
        public void commit() throws SQLException {

        }

        @Override
        public void rollback() throws SQLException {

        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {

        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return false;
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {

        }

        @Override
        public String getCatalog() throws SQLException {
            return null;
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {

        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return 0;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return null;
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

        }

        @Override
        public void setHoldability(int holdability) throws SQLException {

        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return null;
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return null;
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {

        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return null;
        }

        @Override
        public Clob createClob() throws SQLException {
            return null;
        }

        @Override
        public Blob createBlob() throws SQLException {
            return null;
        }

        @Override
        public NClob createNClob() throws SQLException {
            return null;
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return null;
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return false;
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {

        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {

        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return null;
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return null;
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return null;
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return null;
        }

        @Override
        public void setSchema(String schema) throws SQLException {

        }

        @Override
        public String getSchema() throws SQLException {
            return null;
        }

        @Override
        public void abort(Executor executor) throws SQLException {

        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return 0;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
