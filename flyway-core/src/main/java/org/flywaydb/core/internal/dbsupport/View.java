/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

/**
 * Represents a database table within a schema.
 */
public abstract class View extends SchemaObject {

    /**
     * Creates a new view.
     *
     * @param jdbcTemplate
     *            The Jdbc Template for communicating with the DB.
     * @param dbSupport
     *            The database-specific support.
     * @param schema
     *            The schema this table lives in.
     * @param name
     *            The name of the view.
     */
    public View(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }

    /**
     * Checks whether this view exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean exists() {
        try {
            return doExists();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether view " + this + " exists", e);
        }
    }

    /**
     * Checks whether this view exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     * @throws SQLException
     *             when the check failed.
     */
    protected abstract boolean doExists() throws SQLException;

    /**
     * Checks whether the database contains a view matching these criteria.
     *
     * @param catalog
     *            The catalog where the table resides. (optional)
     * @param schema
     *            The schema where the table resides. (optional)
     * @param view
     *            The name of the view. (optional)
     * @param viewTypes
     *            The types of view to look for (ex.: materialized, non-meterialized). (optional)
     * @return {@code true} if a matching table has been found, {@code false} if not.
     * @throws SQLException
     *             when the check failed.
     */
    protected boolean exists(Schema catalog, Schema schema, String view, String... viewTypes) throws SQLException {
        String[] types = viewTypes;
        if (types.length == 0) {
            types = null;
        }

        ResultSet resultSet = null;
        boolean found;
        try {
            resultSet = jdbcTemplate.getMetaData().getTables(
                    catalog == null ? null : catalog.getName(),
                    schema == null ? null : schema.getName(),
                    view,
                    types);
            found = resultSet.next();
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return found;
    }

    /**
     * Checks whether the view has a primary key.
     *
     * @return {@code false}.
     */
    public boolean hasPrimaryKey() {
        return false;
    }

    /**
     * Checks whether the database contains a column matching these criteria.
     *
     * @param column
     *            The column to look for.
     * @return {@code true} if a matching column has been found, {@code false} if not.
     */
    public boolean hasColumn(String column) {
        ResultSet resultSet = null;
        boolean found;
        try {
            if (this.dbSupport.catalogIsSchema()) {
                resultSet = jdbcTemplate.getMetaData().getColumns(this.schema.getName(), null, this.name, column);
            } else {
                resultSet = jdbcTemplate.getMetaData().getColumns(null, this.schema.getName(), this.name, column);
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
     * @param column
     *            The column to look for.
     * @return The size (in characters).
     */
    public int getColumnSize(String column) {
        ResultSet resultSet = null;
        int columnSize;
        try {
            if (this.dbSupport.catalogIsSchema()) {
                resultSet = jdbcTemplate.getMetaData().getColumns(this.schema.getName(), null, this.name, column);
            } else {
                resultSet = jdbcTemplate.getMetaData().getColumns(null, this.schema.getName(), this.name, column);
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
     * Dummy implementation of a View drop. Vendor specific View implementations should override this.
     */
    @Override
    protected void doDrop() throws SQLException {
    }

}
