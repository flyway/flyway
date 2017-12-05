/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
            lock();
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to acquire PostgreSQL advisory lock", e);
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
                LOG.error("Unable to release PostgreSQL advisory lock", e);
            }
        }
    }

    private void lock() throws SQLException {
        while (!tryLock()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new FlywayException("Interrupted while attempting to acquire PostgreSQL advisory lock", e);
            }
        }
    }

    private boolean tryLock() throws SQLException {
        List<Boolean> results = jdbcTemplate.query(
                "SELECT pg_try_advisory_lock(" + lockNum + ")",
                new RowMapper<Boolean>() {
                    @Override
                    public Boolean mapRow(ResultSet rs) throws SQLException {
                        return "t".equals(rs.getString("pg_try_advisory_lock"));
                    }
                });
        return results.size() == 1 && results.get(0);
    }
}