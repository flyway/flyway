/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.util.SqlCallable;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Random;

/**
 * CockroachDB-specific table.
 *
 * Note that CockroachDB doesn't support table locks. We therefore use a row in the schema history as a lock indicator;
 * if another process ahs inserted such a row we wait (potentially indefinitely) for it to be removed before
 * carrying out a migration.
 */
public class CockroachDBTable extends Table<CockroachDBDatabase, CockroachDBSchema> {
    private static final Log LOG = LogFactory.getLog(CockroachDBTable.class);

    /**
     * A random string, used as an ID of this instance of Flyway.
     */
    private String tableLockString = RandomStringGenerator.getNextRandomString();

    /**
     * Creates a new CockroachDB table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    CockroachDBTable(JdbcTemplate jdbcTemplate, CockroachDBDatabase database, CockroachDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        new CockroachDBRetryingStrategy().execute(new SqlCallable<Integer>() {
            @Override
            public Integer call() throws SQLException {
                doDropOnce();
                return null;
            }
        });
    }

    protected void doDropOnce() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return new CockroachDBRetryingStrategy().execute(new SqlCallable<Boolean>() {
            @Override
            public Boolean call() throws SQLException {
                return doExistsOnce();
            }
        });
    }

    protected boolean doExistsOnce() throws SQLException {
        if (schema.cockroachDB1) {
            return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                    "   SELECT 1\n" +
                    "   FROM   information_schema.tables \n" +
                    "   WHERE  table_schema = ?\n" +
                    "   AND    table_name = ?\n" +
                    ")", schema.getName(), name);
        }

        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                "   SELECT 1\n" +
                "   FROM   information_schema.tables \n" +
                "   WHERE  table_catalog = ?\n" +
                "   AND    table_schema = 'public'\n" +
                "   AND    table_name = ?\n" +
                ")", schema.getName(), name);
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

    private boolean insertLockingRow() {
        // Insert the locking row - the primary keyness of installed_rank will prevent us having two.
        Results results = jdbcTemplate.executeStatement("INSERT INTO " + this + " VALUES (-100, '" + tableLockString + "', 'flyway-lock', '', '', 0, '', now(), 0, TRUE)");
        // Succeeded if one row updated and no errors.
        return (results.getResults().size() > 0
                && results.getResults().get(0).getUpdateCount() == 1
                && results.getErrors().size() == 0);
    }

    @Override
    protected void doUnlock() throws SQLException {
        // Leave the locking row alone until we get to the final level of unlocking
        if (lockDepth > 1) {
            return;
        }

        // Check that there are no other locks in place. This should not happen!
        int competingLocksTaken = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this + " WHERE version != '" + tableLockString + "' AND DESCRIPTION = 'flyway-lock'");
        if (competingLocksTaken > 0) {
            throw new FlywayException("Internal error: on unlocking, a competing lock was found");
        }

        // Remove the locking row
        jdbcTemplate.executeStatement("DELETE FROM " + this + " WHERE version = '" + tableLockString + "' AND DESCRIPTION = 'flyway-lock'");
    }
}

class RandomStringGenerator {
    static final Random random = new Random();

    //get next random string
    public static String getNextRandomString(){
        BigInteger bInt = new BigInteger(128, random);
        return bInt.toString(16);
    }
}