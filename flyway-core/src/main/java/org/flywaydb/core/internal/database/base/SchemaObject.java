/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * An object within a database schema.
 */
public abstract class SchemaObject<D extends Database, S extends Schema> {
    /**
     * The Jdbc Template for communicating with the DB.
     */
    protected final JdbcTemplate jdbcTemplate;

    /**
     * The database-specific support.
     */
    protected final D database;

    /**
     * The schema this table lives in.
     */
    protected final S schema;

    /**
     * The name of the table.
     */
    protected final String name;

    /**
     * Creates a new schema object with this name within this schema.
     *
     * @param jdbcTemplate The jdbc template to access the DB.
     * @param database    The database-specific support.
     * @param schema       The schema the object lives in.
     * @param name         The name of the object.
     */
    SchemaObject(JdbcTemplate jdbcTemplate, D database, S schema, String name) {
        this.name = name;
        this.jdbcTemplate = jdbcTemplate;
        this.database = database;
        this.schema = schema;
    }

    /**
     * @return The schema this object lives in.
     */
    public final Schema getSchema() {
        return schema;
    }

    /**
     * @return The name of the object.
     */
    public final String getName() {
        return name;
    }

    /**
     * Drops this object from the database.
     */
    public final void drop() {
        try {
            doDrop();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to drop " + this, e);
        }
    }

    /**
     * Drops this object from the database.
     *
     * @throws java.sql.SQLException when the drop failed.
     */
    protected abstract void doDrop() throws SQLException;

    @Override
    public String toString() {
        return database.quote(schema.getName(), name);
    }
}