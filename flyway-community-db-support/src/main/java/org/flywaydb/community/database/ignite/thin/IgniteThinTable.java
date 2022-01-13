/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.ignite.thin;

import java.sql.SQLException;
import java.util.UUID;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

/**
 * Ignite Thin-specific table.
 */
public class IgniteThinTable extends Table<IgniteThinDatabase, IgniteThinSchema> {
    private static final Log LOG = LogFactory.getLog(IgniteThinTable.class);

    private final String tableLockString = UUID.randomUUID().toString();

    /**
     * Creates a new Ignite table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    public IgniteThinTable(JdbcTemplate jdbcTemplate, IgniteThinDatabase database, IgniteThinSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
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
        if (lockDepth > 0) {
            // Lock has already been taken - so the relevant row in the table already exists
            return;
        }
        int retryCount = 0;
        do {
            try {
                if (insertLockingRow()) {
                    return;
                }
                retryCount++;
                LOG.debug("Waiting for lock on " + this);
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // Ignore - if interrupted, we still need to wait for lock to become available
            }
        } while (retryCount < 50);
        throw new FlywayException("Unable to obtain table lock - another Flyway instance may be running");
    }

    @Override
    protected void doUnlock() throws SQLException {
        // Leave the locking row alone until we get to the final level of unlocking
        if (lockDepth > 1) {
            return;
        }
        // Check that there are no other locks in place. This should not happen!
        int competingLocksTaken = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this + " WHERE " + database.quote("version") + " != '" + tableLockString + "' AND " +
                                                                   database.quote("description") + " = 'flyway-lock'");
        if (competingLocksTaken > 0) {
            throw new FlywayException("Internal error: on unlocking, a competing lock was found");
        }
        // Remove the locking row
        jdbcTemplate.executeStatement("DELETE FROM " + this + " WHERE " + database.quote("version") + " = '" + tableLockString + "' AND " +
                                              database.quote("description") + " = 'flyway-lock'");
    }

    private boolean insertLockingRow() {
        // Insert the locking row - the primary keyness of installed_rank will prevent us having two.
        Results results = jdbcTemplate.executeStatement("INSERT INTO " + this + " VALUES (-100, '" + tableLockString + "', 'flyway-lock', '', '', 0, '', now(), 0, TRUE)");
        // Succeeded if no errors.
        return results.getException() == null;
    }
}