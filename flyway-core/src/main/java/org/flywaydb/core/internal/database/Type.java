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

import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

/**
 * A user defined type within a schema.
 */
public abstract class Type extends SchemaObject {
    /**
     * Creates a new type with this name within this schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param schema       The schema this type lives in.
     * @param name         The name of the type.
     */
    public Type(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }
}
