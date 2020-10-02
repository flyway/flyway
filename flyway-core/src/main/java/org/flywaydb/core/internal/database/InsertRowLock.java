/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
        while (true) {
            try {
                if (insertLockingRow(jdbcTemplate, insertStatementTemplate, booleanTrue)) return;
                if (retryCount < 50) {
                    retryCount++;
                    LOG.debug("Waiting for lock on " + this);
                } else {
                    LOG.error("Waiting for lock on " + this + ". Application may be deadlocked. Lock row may require manual removal " +
                            "from the schema history table.");
                }
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // Ignore - if interrupted, we still need to wait for lock to become available
            }
        }
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

        // Succeeded if no errors.
        return results.getException() == null;
    }

    public void doUnlock(JdbcTemplate jdbcTemplate, String deleteLockTemplate) throws SQLException {
        String deleteLock = String.format(deleteLockTemplate.replace("?", "%s"), tableLockString);

        // Remove the locking row
        jdbcTemplate.executeStatement(deleteLock);
    }

    private String getNextRandomString(){
        BigInteger bInt = new BigInteger(128, random);
        return bInt.toString(16);
    }
}