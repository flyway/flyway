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

import java.sql.SQLException;

/**
 * Represents a database schema.
 */
public abstract class Schema {
    /**
     * The Jdbc Template for communicating with the DB.
     */
    protected final JdbcTemplate jdbcTemplate;

    /**
     * The database-specific support.
     */
    protected final DbSupport dbSupport;

    /**
     * The name of the schema.
     */
    protected final String name;

    /**
     * Creates a new schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public Schema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.name = name;
    }

    /**
     * @return The name of the schema, quoted for the database it lives in.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether this schema exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     */
    public boolean exists() {
        try {
            return doExists();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether schema " + this + " exists", e);
        }
    }

    /**
     * Checks whether this schema exists.
     *
     * @return {@code true} if it does, {@code false} if not.
     * @throws SQLException when the check failed.
     */
    protected abstract boolean doExists() throws SQLException;

    /**
     * Checks whether this schema is empty.
     *
     * @return {@code true} if it is, {@code false} if isn't.
     */
    public boolean empty() {
        try {
            return doEmpty();
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether schema " + this + " is empty", e);
        }
    }

    /**
     * Checks whether this schema is empty.
     *
     * @return {@code true} if it is, {@code false} if isn't.
     * @throws SQLException when the check failed.
     */
    protected abstract boolean doEmpty() throws SQLException;

    /**
     * Creates this schema in the database.
     */
    public void create() {
        try {
            doCreate();
        } catch (SQLException e) {
            throw new FlywayException("Unable to create schema " + this, e);
        }
    }

    /**
     * Creates this schema in the database.
     *
     * @throws SQLException when the creation failed.
     */
    protected abstract void doCreate() throws SQLException;

    /**
     * Drops this schema from the database.
     */
    public void drop() {
        try {
            doDrop();
        } catch (SQLException e) {
            throw new FlywayException("Unable to drop schema " + this, e);
        }
    }

    /**
     * Drops this schema from the database.
     *
     * @throws SQLException when the drop failed.
     */
    protected abstract void doDrop() throws SQLException;

    /**
     * Cleans all the objects in this schema.
     */
    public void clean() {
        try {
            doClean();
        } catch (SQLException e) {
            throw new FlywayException("Unable to clean schema " + this, e);
        }
    }

    /**
     * Cleans all the objects in this schema.
     *
     * @throws SQLException when the clean failed.
     */
    protected abstract void doClean() throws SQLException;

    /**
     * Retrieves all the tables in this schema.
     *
     * @return All tables in the schema.
     */
    public Table[] allTables() {
        try {
            return doAllTables();
        } catch (SQLException e) {
            throw new FlywayException("Unable to retrieve all tables in schema " + this, e);
        }
    }

    /**
     * Retrieves all the tables in this schema.
     *
     * @return All tables in the schema.
     * @throws SQLException when the retrieval failed.
     */
    protected abstract Table[] doAllTables() throws SQLException;

    /**
     * Retrieves the table with this name in this schema.
     *
     * @param tableName The name of the table.
     * @return The table.
     */
    public abstract Table getTable(String tableName);

    @Override
    public String toString() {
        return dbSupport.quote(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;
        return name.equals(schema.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
