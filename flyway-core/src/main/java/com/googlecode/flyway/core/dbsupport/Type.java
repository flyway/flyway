package com.googlecode.flyway.core.dbsupport;

/**
 * A user defined type within a schema.
 */
public abstract class Type extends SchemaObject {
    /**
     * Creates a new type with this name within this schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this type lives in.
     * @param name         The name of the type.
     */
    public Type(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }
}
