/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import lombok.CustomLog;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@CustomLog
public class InsertRowLock {
    private static final Random random = new Random();
    private static final int NUM_THREADS = 2;

    /**
     * A random string used as an ID for this instance of Flyway.
     */
    private final String tableLockString = getNextRandomString();
    private final JdbcTemplate jdbcTemplate;
    private final int lockTimeoutMins;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledFuture;

    public InsertRowLock(JdbcTemplate jdbcTemplate, int lockTimeoutMins) {
        this.jdbcTemplate = jdbcTemplate;
        this.lockTimeoutMins = lockTimeoutMins;
        this.executor = createScheduledExecutor();
    }

    public void doLock(String insertStatementTemplate, String updateLockStatement, String deleteExpiredLockStatement, String booleanTrue) throws SQLException {
        int retryCount = 0;
        while (true) {
            try {
                jdbcTemplate.execute(generateDeleteExpiredLockStatement(deleteExpiredLockStatement));
                if (insertLockingRow(insertStatementTemplate, booleanTrue)) {
                    scheduledFuture = startLockWatchingThread(String.format(updateLockStatement.replace("?", "%s"), tableLockString));
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

    private String generateDeleteExpiredLockStatement(String deleteExpiredLockStatementTemplate) {
        LocalDateTime zonedDateTime = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(lockTimeoutMins);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return String.format(deleteExpiredLockStatementTemplate.replace("?", "%s"), zonedDateTime.format(formatter));
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

    private ScheduledExecutorService createScheduledExecutor() {
        return Executors.newScheduledThreadPool(NUM_THREADS, r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
    }

    private ScheduledFuture<?> startLockWatchingThread(String updateLockStatement) {
        Runnable lockUpdatingTask = () -> {
            LOG.debug("Updating lock in Flyway schema history table");
            jdbcTemplate.executeStatement(updateLockStatement);
        };
        return executor.scheduleAtFixedRate(lockUpdatingTask, 0, lockTimeoutMins / 2, TimeUnit.MINUTES);
    }

    private void stopLockWatchingThread() {
        scheduledFuture.cancel(true);
    }
}