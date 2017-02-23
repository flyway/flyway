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
package org.flywaydb.core.internal.dbsupport.postgresql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Spring-like template for executing with PostgreSQL advisory locks.
 */
public class PostgreSQLAdvisoryLockTemplate {
    private static final Log LOG = LogFactory.getLog(PostgreSQLAdvisoryLockTemplate.class);

    private static final long LOCK_MAGIC_NUM =
            (0x46L << 40) // F
                    + (0x6CL << 32) // l
                    + (0x79L << 24) // y
                    + (0x77 << 16) // w
                    + (0x61 << 8) // a
                    + 0x79; // y

    /**
     * The connection for the advisory lock.
     */
    private final JdbcTemplate jdbcTemplate;

    private final long lockNum;

    /**
     * Creates a new advisory lock template for this connection.
     *
     * @param jdbcTemplate The jdbcTemplate for the connection.
     * @param discriminator A number to discriminate between locks.
     */
    PostgreSQLAdvisoryLockTemplate(JdbcTemplate jdbcTemplate, int discriminator) {
        this.jdbcTemplate = jdbcTemplate;
        lockNum = LOCK_MAGIC_NUM + discriminator;
    }

    /**
     * Executes this callback with an advisory lock.
     *
     * @param callable The callback to execute.
     * @return The result of the callable code.
     */
    public <T> T execute(Callable<T> callable) {
        try {
            jdbcTemplate.execute("SELECT pg_advisory_lock(" + lockNum + ")");
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to acquire Flyway advisory lock", e);
        } catch (Exception e) {
            RuntimeException rethrow;
            if (e instanceof RuntimeException) {
                rethrow = (RuntimeException) e;
            } else {
                rethrow = new FlywayException(e);
            }
            throw rethrow;
        } finally {
            try {
                jdbcTemplate.execute("SELECT pg_advisory_unlock(" + lockNum + ")");
            } catch (SQLException e) {
                LOG.error("Unable to release Flyway advisory lock", e);
            }
        }
    }
}
