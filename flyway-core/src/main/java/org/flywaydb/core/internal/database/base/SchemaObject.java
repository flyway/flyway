/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

public abstract class SchemaObject<D extends Database, S extends Schema> {
    protected final JdbcTemplate jdbcTemplate;
    protected final D database;
    protected final S schema;
    protected final String name;

    /**
     * @param jdbcTemplate The JDBC template to access the DB.
     * @param database The database-specific support.
     * @param schema The schema the object lives in.
     * @param name The name of the object.
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
    public final S getSchema() {
        return schema;
    }

    public final D getDatabase() {
        return database;
    }

    public final String getName() {
        return name;
    }

    public final void drop() {
        try {
            doDrop();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to drop " + this, e);
        }
    }

    /**
     * @throws java.sql.SQLException when the drop failed.
     */
    protected abstract void doDrop() throws SQLException;

    @Override
    public String toString() {
        return database.quote(schema.getName(), name);
    }
}