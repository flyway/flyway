/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a database table within a schema.
 */
public abstract class Table {
    private static final Log LOG = LogFactory.getLog(Table.class);

    /**
     * The Jdbc Template for communicating with the DB.
     */
    protected final JdbcTemplate jdbcTemplate;

    /**
     * The database-specific support.
     */
    protected final DbSupport dbSupport;

    /**
     * The schema this table lives in.
     */
    protected final Schema schema;

    /**
     * The name of the table.
     */
    protected final String name;

    /**
     * Creates a new table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public Table(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.schema = schema;
        this.name = name;
    }

    /**
     * @return The schema this table lives in.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @return The name of the table.
     */
    public String getName() {
        return name;
    }

    /**
     * Drops this table from the database.
     */
    public void drop() {
        try {
            doDrop();
        } catch (SQLException e) {
            throw new FlywayException("Unable to drop table " + this, e);
        }
    }

    /**
     * Drops this table from the database.
     *
     * @throws java.sql.SQLException when the drop failed.
     */
    protected abstract void doDrop() throws SQLException;

    /**
     * Checks whether this table exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean exists() {
        try {
            return doExists();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether table " + this + " exists", e);
        }
    }

    /**
     * Checks whether this table exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     * @throws SQLException when the check failed.
     */
    protected abstract boolean doExists() throws SQLException;

    /**
     * Checks whether this table is already present in the database. WITHOUT quoting either the table or the schema name!
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    public boolean existsNoQuotes() {
        try {
            return doExistsNoQuotes();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether table " + this + " exists", e);
        }
    }

    /**
     * Checks whether this table is already present in the database. WITHOUT quoting either the table or the schema name!
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     * @throws SQLException when there was an error checking whether this table exists in this schema.
     */
    protected abstract boolean doExistsNoQuotes() throws SQLException;

    /**
     * Checks whether the database contains a table matching these criteria.
     *
     * @param catalog    The catalog where the table resides. (optional)
     * @param schema     The schema where the table resides. (optional)
     * @param table      The name of the table. (optional)
     * @param tableTypes The types of table to look for (ex.: TABLE). (optional)
     * @return {@code true} if a matching table has been found, {@code false} if not.
     * @throws SQLException when the check failed.
     */
    protected boolean exists(Schema catalog, Schema schema, String table, String... tableTypes) throws SQLException {
        String[] types = tableTypes;
        if (types.length == 0) {
            types = null;
        }

        ResultSet resultSet = null;
        boolean found;
        try {
            resultSet = jdbcTemplate.getMetaData().getTables(
                    catalog == null ? null : catalog.getName(),
                    schema == null ? null : schema.getName(),
                    table,
                    types);
            found = resultSet.next();
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return found;
    }

    /**
     * Checks whether the table has a primary key.
     *
     * @return {@code true} if a primary key has been found, {@code false} if not.
     */
    public boolean hasPrimaryKey() {
        ResultSet resultSet = null;
        boolean found;
        try {
            if (dbSupport.catalogIsSchema()) {
                resultSet = jdbcTemplate.getMetaData().getPrimaryKeys(schema.getName(), null, name);
            } else {
                resultSet = jdbcTemplate.getMetaData().getPrimaryKeys(null, schema.getName(), name);
            }
            found = resultSet.next();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether table " + this + " has a primary key", e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return found;
    }

    /**
     * Checks whether the database contains a column matching these criteria.
     *
     * @param column The column to look for.
     * @return {@code true} if a matching column has been found, {@code false} if not.
     */
    public boolean hasColumn(String column) {
        ResultSet resultSet = null;
        boolean found;
        try {
            if (dbSupport.catalogIsSchema()) {
                resultSet = jdbcTemplate.getMetaData().getColumns(schema.getName(), null, name, column);
            } else {
                resultSet = jdbcTemplate.getMetaData().getColumns(null, schema.getName(), name, column);
            }
            found = resultSet.next();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether table " + this + " has a column named " + column, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return found;
    }

    /**
     * Determines the size (in characters) of this column.
     *
     * @param column The column to look for.
     * @return The size (in characters).
     */
    public int getColumnSize(String column) {
        ResultSet resultSet = null;
        int columnSize;
        try {
            if (dbSupport.catalogIsSchema()) {
                resultSet = jdbcTemplate.getMetaData().getColumns(schema.getName(), null, name, column);
            } else {
                resultSet = jdbcTemplate.getMetaData().getColumns(null, schema.getName(), name, column);
            }
            resultSet.next();
            columnSize = resultSet.getInt("COLUMN_SIZE");
        } catch (SQLException e) {
            throw new FlywayException("Unable to check the size of column " + column + " in table " + this, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return columnSize;
    }

    /**
     * Locks this table in this schema using a read/write pessimistic lock until the end of the current transaction.
     */
    public void lock() {
        try {
            LOG.debug("Locking table " + this + "...");
            doLock();
            LOG.debug("Lock acquired for table " + this);
        } catch (SQLException e) {
            throw new FlywayException("Unable to lock table " + this, e);
        }
    }

    /**
     * Locks this table in this schema using a read/write pessimistic lock until the end of the current transaction.
     *
     * @throws SQLException when this table in this schema could not be locked.
     */
    protected abstract void doLock() throws SQLException;

    @Override
    public String toString() {
        return dbSupport.quote(schema.getName(), name);
    }
}
