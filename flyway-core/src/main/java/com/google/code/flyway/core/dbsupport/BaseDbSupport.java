package com.google.code.flyway.core.dbsupport;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Common base class for all database-specific support classes.
 */
public abstract class BaseDbSupport implements DbSupport {
    /**
     * The simpleJdbcTemplate to use for accessing the database.
     */
    protected final SimpleJdbcTemplate simpleJdbcTemplate;

    /**
     * The name of the schema to operate on.
     */
    protected final String schemaName;

    /**
     * Initializes the DbSupport class with this simpleJdbcTemplate for this schema.
     *
     * @param simpleJdbcTemplate The simpleJdbcTemplate to use for accessing the database.
     * @param schemaName The name of the schema to operate on.
     */
    public BaseDbSupport(SimpleJdbcTemplate simpleJdbcTemplate, String schemaName) {
        this.simpleJdbcTemplate = simpleJdbcTemplate;
        this.schemaName = schemaName;
    }
}
