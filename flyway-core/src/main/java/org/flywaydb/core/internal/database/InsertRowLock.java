/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.database;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Table;
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
import java.util.function.BiPredicate;

/**
 * Distributed locking mechanism using row insertion in the Flyway schema history table.
 *
 * <p>This class implements a database-based locking strategy to prevent multiple Flyway instances
 * from simultaneously migrating the same database. The lock is implemented by inserting a special
 * row into the schema history table with a fixed installed_rank value (-100). The primary key
 * constraint on installed_rank ensures only one instance can hold the lock at a time.</p>
 *
 * <p>The lock is kept alive by a background thread that periodically updates the lock row's timestamp.
 * Locks that haven't been updated within {@link #LOCK_TIMEOUT_MINS} are considered expired and can be
 * cleaned up by other instances attempting to acquire the lock.</p>
 */
@CustomLog
public class InsertRowLock {
    private static final Random random = new Random();
    private static final int NUM_THREADS = 2;

    /**
     * A random string used as an ID for this instance of Flyway.
     */
    private final String tableLockString = getNextRandomString();
    private final JdbcTemplate jdbcTemplate;
    public static final int LOCK_TIMEOUT_MINS = 10;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledFuture;
    /**
     * Description field value used in the lock row to identify it as a Flyway lock.
     */
    public static String FLYWAY_LOCK_STRING = "flyway-lock";

    public InsertRowLock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.executor = createScheduledExecutor();
    }

    /**
     * Gets the unique lock identifier for this Flyway instance.
     *
     * @return the unique lock identifier stored in the lock row's 'version' column
     */
    public String getLockId() {
        return this.tableLockString;
    }

    /**
     * Acquires a lock on the schema history table using primary key constraint.
     *
     * <p>This method assumes the database supports primary key constraints on 'installed_rank'.
     * The lock is acquired by inserting a row with installed_rank = -100. If another instance
     * already holds the lock, this method will retry every second until the lock becomes available.</p>
     *
     * @param insertStatementTemplate    template for INSERT statement with placeholders ({@link org.flywaydb.core.internal.database.base.Database#getInsertStatement(Table)})
     * @param updateLockStatement        SQL statement to update the lock timestamp, takes a single '?' placeholder for the 'version' column that will be replaced with {@link #tableLockString}
     * @param deleteExpiredLockStatement SQL statement to delete expired locks, takes a single '?' placeholder that will be replaced with a timestamp to remove locks older than the cutoff
     * @param booleanTrue                database-specific representation of boolean true value
     * @throws SQLException if a database error occurs
     */
    public void doLock(String insertStatementTemplate, String updateLockStatement, String deleteExpiredLockStatement, String booleanTrue) throws SQLException {
        doLock(insertStatementTemplate, updateLockStatement, deleteExpiredLockStatement, "0", booleanTrue, (jdbcTemplate, insertStatement) -> {
            // Insert the locking row - the primary key-ness of 'installed_rank' will prevent us having two
            Results results = jdbcTemplate.executeStatement(insertStatement);

            // Succeed if there were no errors
            return results.getException() == null;
        });
    }

    /**
     * Acquires a lock on the schema history table using a custom locking strategy.
     *
     * <p>This is the main locking method that supports different database-specific locking strategies
     * through the lockStrategy predicate. The method will:</p>
     * <ol>
     *   <li>Delete any expired locks from previous instances</li>
     *   <li>Attempt to insert a lock row using the provided strategy</li>
     *   <li>If successful, start a background thread to keep the lock alive</li>
     *   <li>If unsuccessful, retry every second (up to 50 retries with debug logging, then error logging)</li>
     * </ol>
     *
     * @param insertStatementTemplate    template for INSERT statement with placeholders ({@link org.flywaydb.core.internal.database.base.Database#getInsertStatement(Table)})
     * @param updateLockStatement        SQL statement to update the lock timestamp, takes a single '?' placeholder for the 'version' column that will be replaced with {@link #tableLockString}
     * @param deleteExpiredLockStatement SQL statement to delete expired locks, takes a single '?' placeholder that will be replaced with a timestamp to remove locks older than the cutoff
     * @param checksumValue              checksum value to use in the lock row
     * @param booleanTrue                database-specific representation of boolean true value
     * @param lockStrategy               predicate that attempts to acquire the lock and returns true on success. Takes (JdbcTemplate, String insertStatement) where the statement is the formatted INSERT for the lock row
     * @throws SQLException if a database error occurs
     */
    public void doLock(String insertStatementTemplate,
                       String updateLockStatement,
                       String deleteExpiredLockStatement,
                       String checksumValue,
                       String booleanTrue,
                       BiPredicate<JdbcTemplate, String> lockStrategy) throws SQLException {
        int retryCount = 0;
        while (true) {
            try {
                jdbcTemplate.execute(generateDeleteExpiredLockStatement(deleteExpiredLockStatement));
                if (insertLockingRow(insertStatementTemplate, checksumValue, booleanTrue, lockStrategy)) {
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
        LocalDateTime zonedDateTime = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(LOCK_TIMEOUT_MINS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return String.format(deleteExpiredLockStatementTemplate.replace("?", "%s"), zonedDateTime.format(formatter));
    }

    private boolean insertLockingRow(String insertStatementTemplate, String checksumValue, String booleanTrue, BiPredicate<JdbcTemplate, String> lockStrategy) {
        String insertStatement = String.format(insertStatementTemplate.replace("?", "%s"),
                                               -100,
                                               "'" + tableLockString + "'",
                                               "'" + FLYWAY_LOCK_STRING + "'",
                                               "''",
                                               "''",
                                               checksumValue,
                                               "''",
                                               0,
                                               booleanTrue
        );
        return lockStrategy.test(jdbcTemplate, insertStatement);
    }

    public void doUnlock(String deleteLockTemplate) throws SQLException {
        stopLockWatchingThread();
        String deleteLockStatement = String.format(deleteLockTemplate.replace("?", "%s"), tableLockString);
        jdbcTemplate.execute(deleteLockStatement);
    }

    private String getNextRandomString() {
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
        return executor.scheduleAtFixedRate(lockUpdatingTask, 0, LOCK_TIMEOUT_MINS / 2, TimeUnit.MINUTES);
    }

    private void stopLockWatchingThread() {
        scheduledFuture.cancel(true);
    }
}
