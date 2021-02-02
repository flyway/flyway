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
import java.time.Duration;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class InsertRowLock {
    private static final Log LOG = LogFactory.getLog(InsertRowLock.class);
    private static final Random random = new Random();

    /**
     * A random string used as an ID for this instance of Flyway.
     */
    private final String tableLockString = getNextRandomString();
    private final JdbcTemplate jdbcTemplate;
    public final int lockTimeoutMins;
    private Timer timer;

    public InsertRowLock(JdbcTemplate jdbcTemplate, int lockTimeoutMins) {
        this.jdbcTemplate = jdbcTemplate;
        this.lockTimeoutMins = lockTimeoutMins;
    }

    public void doLock(String insertStatementTemplate, String updateLockStatement, String deleteExpiredLockStatement, String booleanTrue) throws SQLException {
        int retryCount = 0;
        while (true) {
            try {
                jdbcTemplate.execute(deleteExpiredLockStatement);
                if (insertLockingRow(insertStatementTemplate, booleanTrue)) {
                    startLockWatchingThread(String.format(updateLockStatement.replace("?", "%s"), tableLockString));
                    return;
                }
                if (retryCount < 50) {
                    retryCount++;
                    LOG.debug("Waiting for lock on Flyway schema history table");
                } else {
                    LOG.error("Waiting for lock on Flyway schema history table. Application may be deadlocked. Lock row may require manual removal " +
                            "from the schema history table.");
                }
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // Ignore - if interrupted, we still need to wait for lock to become available
            }
        }
    }

    private boolean insertLockingRow(String insertStatementTemplate, String booleanTrue) {
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

        // Insert the locking row - the primary key-ness of 'installed_rank' will prevent us having two
        Results results = jdbcTemplate.executeStatement(insertStatement);

        // Succeed if there were no errors
        return results.getException() == null;
    }

    public void doUnlock(String deleteLockTemplate) throws SQLException {
        stopLockWatchingThread();
        String deleteLockStatement = String.format(deleteLockTemplate.replace("?", "%s"), tableLockString);
        jdbcTemplate.execute(deleteLockStatement);
    }

    private String getNextRandomString(){
        return new BigInteger(128, random).toString(16);
    }

    private void startLockWatchingThread(String updateLockStatement) {
        TimerTask lockWatcherTask = new TimerTask() {
            @Override
            public void run() {
                LOG.debug("Updating lock in Flyway schema history table");
                jdbcTemplate.executeStatement(updateLockStatement);
            }
        };
        timer = new Timer();
        timer.schedule(lockWatcherTask, 0, Duration.ofMinutes(lockTimeoutMins / 2).toMillis());
    }

    private void stopLockWatchingThread() {
        timer.cancel();
    }
}