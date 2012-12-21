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
package com.googlecode.flyway.core.dbsupport;

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
     * @throws SQLException when the check failed.
     */
    public abstract boolean exists() throws SQLException;

    /**
     * Checks whether this schema is empty.
     *
     * @return {@code true} if it is, {@code false} if isn't.
     * @throws SQLException when the check failed.
     */
    public abstract boolean empty() throws SQLException;

    /**
     * Creates this schema in the database.
     * @throws SQLException when the creation failed.
     */
    public abstract void create() throws SQLException;

    /**
     * Drops this schema from the database.
     * @throws SQLException when the drop failed.
     */
    public abstract void drop() throws SQLException;

    /**
     * Cleans all the objects in this schema.
     * @throws SQLException when the clean failed.
     */
    public abstract void clean() throws SQLException;

    /**
     * Retrieves all the tables in this schema.
     *
     * @return All tables in the schema.
     * @throws SQLException when the retrieval failed.
     */
    public abstract Table[] allTables() throws SQLException;

    @Override
    public String toString() {
        return name;
    }
}
