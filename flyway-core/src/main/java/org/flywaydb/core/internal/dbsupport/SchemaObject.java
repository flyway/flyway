/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;

import java.sql.SQLException;

/**
 * An object within a database schema.
 */
public abstract class SchemaObject {
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
     * Creates a new schema object with this name within this schema.
     *
     * @param jdbcTemplate The jdbc template to access the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema the object lives in.
     * @param name         The name of the object.
     */
    public SchemaObject(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        this.name = name;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
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
        return dbSupport.quote(schema.getName(), name);
    }
}
