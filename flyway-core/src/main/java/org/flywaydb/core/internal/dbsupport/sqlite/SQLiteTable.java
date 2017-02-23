/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.sqlite;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * SQLite-specific table.
 */
public class SQLiteTable extends Table {
    private static final Log LOG = LogFactory.getLog(SQLiteTable.class);

    /** SQLite system tables are undroppable. */
    static final String SQLITE_SEQUENCE = "sqlite_sequence";
    private final boolean undroppable;

    /**
     * Creates a new SQLite table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public SQLiteTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);
        undroppable = SQLITE_SEQUENCE.equals(name);
    }

    @Override
    protected void doDrop() throws SQLException {
        if (undroppable) {
            LOG.debug("SQLite system table " + this + " cannot be dropped. Ignoring.");
        } else {
            jdbcTemplate.execute("DROP TABLE " + dbSupport.quote(schema.getName(), name));
        }
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(tbl_name) FROM "
                + dbSupport.quote(schema.getName()) + ".sqlite_master WHERE type='table' AND tbl_name='" + name + "'") > 0;
    }

    @Override
    protected void doLock() throws SQLException {
        LOG.debug("Unable to lock " + this + " as SQLite does not support locking. No concurrent migration supported.");
    }
}
