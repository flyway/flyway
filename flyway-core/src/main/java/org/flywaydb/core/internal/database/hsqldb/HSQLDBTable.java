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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;

import java.sql.SQLException;

/**
 * HSQLDB-specific table.
 */
public class HSQLDBTable extends Table {
    private static final Log LOG = LogFactory.getLog(HSQLDBTable.class);

    // [enterprise]
    /**
     * Flag indicating whether we are running against the old Hsql 1.8 instead of the newer 2.x.
     */
    private boolean version18;
    // [/enterprise]

    /**
     * Creates a new Hsql table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    HSQLDBTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);

        // [enterprise]
        version18 = database.getMajorVersion() < 2;
        // [/enterprise]
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        // [enterprise]
        if (version18) {
            LOG.debug("Unable to lock " + this + " as Hsql 1.8 does not support locking. No concurrent migration supported.");
            return;
        }
        // [/enterprise]
        jdbcTemplate.execute("LOCK TABLE " + this + " WRITE");
    }
}
