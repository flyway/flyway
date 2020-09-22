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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Random;

public class InsertRowLock {
    private static final Log LOG = LogFactory.getLog(InsertRowLock.class);
    private static final Random random = new Random();

    /**
     * A random string, used as an ID of this instance of Flyway.
     */
    private final String tableLockString = getNextRandomString();

    public void doLock(JdbcTemplate jdbcTemplate, String insertStatementTemplate, String booleanTrue) throws SQLException {

        int retryCount = 0;
        do {
            try {
                if (insertLockingRow(jdbcTemplate, insertStatementTemplate, booleanTrue)) {
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

    private boolean insertLockingRow(JdbcTemplate jdbcTemplate, String insertStatementTemplate, String booleanTrue) {
        String insertStatement = String.format(insertStatementTemplate.replace("?", "%s"),
                -100,
                "'" + tableLockString + "'",
                "'flyway-lock'",
                "''",
                "''",
                0,
                "''",
                0,
                booleanTrue
        );

        // Insert the locking row - the primary keyness of installed_rank will prevent us having two.
        Results results = jdbcTemplate.executeStatement(insertStatement);
        // Succeeded if one row updated and no errors.
        return (results.getResults().size() > 0
                && results.getResults().get(0).getUpdateCount() == 1
                && results.getErrors().size() == 0);
    }

    public void doUnlock(JdbcTemplate jdbcTemplate, String selectLockTemplate, String deleteLockTemplate) throws SQLException {

        String selectLock = String.format(selectLockTemplate.replace("?", "%s"), tableLockString);

        // Check that there are no other locks in place. This should not happen!
        int competingLocksTaken = jdbcTemplate.queryForInt(selectLock);
        if (competingLocksTaken > 0) {
            throw new FlywayException("Internal error: on unlocking, a competing lock was found");
        }

        String deleteLock = String.format(deleteLockTemplate.replace("?", "%s"), tableLockString);

        // Remove the locking row
        jdbcTemplate.executeStatement(deleteLock);
    }

    //get next random string
    private static String getNextRandomString(){
        BigInteger bInt = new BigInteger(128, random);
        return bInt.toString(16);
    }

}