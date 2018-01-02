/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

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
    protected final Database database;

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
     * @param database    The database-specific support.
     * @param schema       The schema the object lives in.
     * @param name         The name of the object.
     */
    public SchemaObject(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
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
