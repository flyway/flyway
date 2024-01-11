package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public abstract class Type<D extends Database, S extends Schema> extends SchemaObject<D, S> {
    public Type(JdbcTemplate jdbcTemplate, D database, S schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }
}