/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.internal.database.postgresql;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.TransactionalExecutionTemplate;
import org.flywaydb.core.internal.strategy.RetryStrategy;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

@CustomLog
public class PostgreSQLAdvisoryLockTemplate {
    private static final long LOCK_MAGIC_NUM =
            (0x46L << 40) // F
                    + (0x6CL << 32) // l
                    + (0x79L << 24) // y
                    + (0x77 << 16) // w
                    + (0x61 << 8) // a
                    + 0x79; // y

    private final Configuration configuration;
    private final JdbcTemplate jdbcTemplate;
    private final long lockNum;

    PostgreSQLAdvisoryLockTemplate(Configuration configuration, JdbcTemplate jdbcTemplate, int discriminator) {
        this.configuration = configuration;
        this.jdbcTemplate = jdbcTemplate;
        this.lockNum = LOCK_MAGIC_NUM + discriminator;
    }

    public <T> T execute(Callable<T> callable) {
        PostgreSQLConfigurationExtension configurationExtension = configuration.getPluginRegister().getPlugin(PostgreSQLConfigurationExtension.class);

        if (configurationExtension.isTransactionalLock()) {
            return new TransactionalExecutionTemplate(jdbcTemplate.getConnection(), true).execute(() -> execute(callable, this::tryLockTransactional));
        } else {
            RuntimeException rethrow = null;
            try {
                return execute(callable, this::tryLock);
            } catch (RuntimeException e) {
                rethrow = e;
                throw rethrow;
            } finally {
                unlock(rethrow);
            }
        }
    }

    private <T> T execute(Callable<T> callable, SqlCallable<Boolean> tryLock) {
        try {
            lock(tryLock);
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to acquire PostgreSQL advisory lock", e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new FlywayException(e);
        }
    }

    private void lock(SqlCallable<Boolean> tryLock) throws SQLException {
        RetryStrategy strategy = new RetryStrategy();
        strategy.doWithRetries(tryLock, "Interrupted while attempting to acquire PostgreSQL advisory lock",
                               "Number of retries exceeded while attempting to acquire PostgreSQL advisory lock. " +
                                       "Configure the number of retries with the 'lockRetryCount' configuration option: " + FlywayDbWebsiteLinks.LOCK_RETRY_COUNT);
    }

    private boolean tryLockTransactional() throws SQLException {
        List<Boolean> results = jdbcTemplate.query("SELECT pg_try_advisory_xact_lock(" + lockNum + ")", rs -> rs.getBoolean("pg_try_advisory_xact_lock"));
        return results.size() == 1 && results.get(0);
    }

    private boolean tryLock() throws SQLException {
        List<Boolean> results = jdbcTemplate.query("SELECT pg_try_advisory_lock(" + lockNum + ")", rs -> rs.getBoolean("pg_try_advisory_lock"));
        return results.size() == 1 && results.get(0);
    }

    private void unlock(RuntimeException rethrow) throws FlywaySqlException {
        try {
            boolean unlocked = jdbcTemplate.queryForBoolean("SELECT pg_advisory_unlock(" + lockNum + ")");
            if (!unlocked) {
                if (rethrow == null) {
                    throw new FlywayException("Unable to release PostgreSQL advisory lock");
                } else {
                    LOG.error("Unable to release PostgreSQL advisory lock");
                }
            }
        } catch (SQLException e) {
            if (rethrow == null) {
                throw new FlywaySqlException("Unable to release PostgreSQL advisory lock", e);
            } else {
                LOG.error("Unable to release PostgreSQL advisory lock", e);
            }
        }
    }
}