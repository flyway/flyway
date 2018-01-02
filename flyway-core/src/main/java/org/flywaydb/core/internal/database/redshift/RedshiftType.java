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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Type;

import java.sql.SQLException;

/**
 * PostgreSQL-specific type.
 */
public class RedshiftType extends Type {
    /**
     * Creates a new PostgreSQL type.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param schema       The schema this type lives in.
     * @param name         The name of the type.
     */
    public RedshiftType(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}
