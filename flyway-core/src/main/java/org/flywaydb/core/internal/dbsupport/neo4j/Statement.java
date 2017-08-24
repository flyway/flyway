/*
 * Copyright (c) 2016 LARUS Business Automation [http://www.larus-ba.it]
 * <p>
 * This file is part of the "LARUS Integration Framework for Neo4j".
 * <p>
 * The "LARUS Integration Framework for Neo4j" is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Created on 03/02/16
 */
package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

import org.neo4j.jdbc.Connection;
import org.neo4j.jdbc.utils.ExceptionBuilder;

/**
 * @author AgileLARUS
 * @since 3.0.0
 */
public abstract class Statement implements java.sql.Statement {

	protected Connection connection;
	protected ResultSet  currentResultSet;
	protected int        currentUpdateCount;
	private   int        maxRows;
	private   int        queryTimeout;

	/**
	 * Default constructor with JDBC connection.
	 *
	 * @param connection The JDBC connection
	 */
	protected Statement(Connection connection) {
		this.connection = connection;
		this.currentResultSet = null;
		this.currentUpdateCount = -1;

		this.maxRows = 0;
		if (connection != null && connection.getProperties() != null) {
			this.maxRows = Integer.parseInt(connection.getProperties().getProperty("maxrows", "0"));
		}
	}

	/**
	 * Check if this statement is closed or not.
	 * If it is, we throw an exception.
	 * @throws SQLException sqlexception
	 */
	protected void checkClosed() throws SQLException {
		if (this.isClosed()) {
			throw new SQLException("Statement already closed");
		}
	}

	/*------------------------------------*/
	/*       Default implementation       */
	/*------------------------------------*/

	@Override public Connection getConnection() throws SQLException {
		this.checkClosed();
		return this.connection;
	}

	/**
	 * /!\ Like javadoc said, this method should be called one times.
	 * Bolt statement always return two results : ResulSet and updatecount.
	 * Because each time we retrieve a data we set it to the default value
	 * here we have two cases :
	 * - if there a resultset : we tell it by responding -1
	 * - otherwise we give the updatecount and reset its value to default
	 */
	@Override public int getUpdateCount() throws SQLException {
		this.checkClosed();
		int update = this.currentUpdateCount;

		if (this.currentResultSet != null) {
			update = -1;
		} else {
			this.currentUpdateCount = -1;
		}
		return update;
	}

	/**
	 * Like javadoc said, this method should be called one times.
	 */
	@Override public ResultSet getResultSet() throws SQLException {
		this.checkClosed();
		ResultSet rs = this.currentResultSet;
		this.currentResultSet = null;
		return rs;
	}

	@Override public int getMaxRows() throws SQLException {
		this.checkClosed();
		return this.maxRows;
	}

	@Override public void setMaxRows(int max) throws SQLException {
		this.checkClosed();
		this.maxRows = max;
	}

	@Override public boolean isClosed() throws SQLException {
		return !(connection != null && !connection.isClosed());
	}

	@Override public void close() throws SQLException {
		if (!this.isClosed()) {
			if (this.currentResultSet != null && !this.currentResultSet.isClosed()) {
				this.currentResultSet.close();
			}
			this.currentUpdateCount = -1;
			this.connection = null;
		}
	}

//	@Override public <T> T unwrap(Class<T> iface) throws SQLException {
//		return org.neo4j.jdbc.Wrapper.unwrap(iface, this);
//	}
//
//	@Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
//		return org.neo4j.jdbc.Wrapper.isWrapperFor(iface, this.getClass());
//	}

	/**
	 * Some tools call this method, so this just a workaround to make it work.
	 * If you set the fetch at Integer.MIN_VALUE, or if you put maxRows to -1, there is no exception.
	 * It's pretty much the same hack as the mysql connector.
	 */
	@Override public void setFetchSize(int rows) throws SQLException {
		this.checkClosed();
		if (rows != Integer.MIN_VALUE && (this.getMaxRows() > 0 && rows > this.getMaxRows())) {
			throw new UnsupportedOperationException("Not implemented yet. => maxRow :" + getMaxRows() + " rows :" + rows);
		}
	}

	/**
	 * Some tools call this method, so for now just respond false.
	 */
	@Override public boolean getMoreResults() throws SQLException {
		this.checkClosed();
		return !( this.currentResultSet == null && this.currentUpdateCount == -1 );
	}

	/**
	 * Some tool call this method, so for now just respond  null.
	 * It will be possible to implement this an explain.
	 */
	@Override public SQLWarning getWarnings() throws SQLException {
		this.checkClosed();
		return null;
	}

	/**
	 * Some tool call this method.
	 */
	@Override public void clearWarnings() throws SQLException {
		this.checkClosed();
		// nothing yet
	}

	/**
	 * Added just for value memorization
	 * @return the current query timeout limit in seconds; zero means there is no limit
	 * @throws SQLException if a database error occurs
	 */
	@Override public int getQueryTimeout() throws SQLException {
		return this.queryTimeout;
	}

	/**
	 * Added just for value memorization
   * @param seconds the new query timeout limit in seconds; zero means there is no limit
   * @exception SQLException if a database error occurs
	 */
	@Override public void setQueryTimeout(int seconds) throws SQLException {
		this.queryTimeout = seconds;
	}

	/*-----------------------------*/
	/*       Abstract method       */
	/*-----------------------------*/

	@Override public abstract boolean execute(String sql) throws SQLException;

	@Override public abstract ResultSet executeQuery(String sql) throws SQLException;

	@Override public abstract int executeUpdate(String sql) throws SQLException;

	@Override public abstract int getResultSetHoldability() throws SQLException;

	@Override public abstract int getResultSetConcurrency() throws SQLException;

	@Override public abstract int getResultSetType() throws SQLException;

	/*---------------------------------*/
	/*       Not implemented yet       */
	/*---------------------------------*/

	@Override public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public boolean execute(String sql, String[] columnNames) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int getMaxFieldSize() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void setMaxFieldSize(int max) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void setEscapeProcessing(boolean enable) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void setCursorName(String name) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void setFetchDirection(int direction) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int getFetchDirection() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int getFetchSize() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public boolean getMoreResults(int current) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public ResultSet getGeneratedKeys() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void cancel() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void setPoolable(boolean poolable) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public boolean isPoolable() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void addBatch(String sql) throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void clearBatch() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public int[] executeBatch() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public void closeOnCompletion() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

	@Override public boolean isCloseOnCompletion() throws SQLException {
		throw ExceptionBuilder.buildUnsupportedOperationException();
	}

}