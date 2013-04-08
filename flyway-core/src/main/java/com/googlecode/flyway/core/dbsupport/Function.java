package com.googlecode.flyway.core.dbsupport;

/**
 * A user defined type within a schema.
 */
public abstract class Function extends SchemaObject {
    /**
     * The arguments of the function.
     */
    protected String[] args;

    /**
     * Creates a new function with this name within this schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this function lives in.
     * @param name         The name of the function.
     * @param args         The arguments of the function.
     */
    public Function(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name, String... args) {
        super(jdbcTemplate, dbSupport, schema, name);
        this.args = args;
    }
}
