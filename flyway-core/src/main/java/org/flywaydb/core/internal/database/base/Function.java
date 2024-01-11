package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

public abstract class Function<D extends Database, S extends Schema> extends SchemaObject<D, S> {
    protected String[] args;

    public Function(JdbcTemplate jdbcTemplate, D database, S schema, String name, String... args) {
        super(jdbcTemplate, database, schema, name);
        this.args = args == null ? new String[0] : args;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + StringUtils.arrayToCommaDelimitedString(args) + ")";
    }
}