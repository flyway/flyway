/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Spring-like template for executing with SQL Server application locks.
 */
public class SQLServerApplicationLockTemplate {
    private static final Log LOG = LogFactory.getLog(SQLServerApplicationLockTemplate.class);

    private final SQLServerConnection connection;
    private final JdbcTemplate jdbcTemplate;
    private final String databaseName;
    private final String lockName;

    /**
     * Creates a new application lock template for this connection.
     *  @param connection The connection reference.
     * @param jdbcTemplate  The jdbcTemplate for the connection.
     * @param discriminator A number to discriminate between locks.
     */
    SQLServerApplicationLockTemplate(SQLServerConnection connection, JdbcTemplate jdbcTemplate, String databaseName, int discriminator) {
        this.connection = connection;
        this.jdbcTemplate = jdbcTemplate;
        this.databaseName = databaseName;
        lockName = "Flyway-" + discriminator;
    }

    /**
     * Executes this callback with an advisory lock.
     *
     * @param callable The callback to execute.
     * @return The result of the callable code.
     */
    public <T> T execute(Callable<T> callable) {
        try {
            connection.setCurrentDatabase(databaseName);
            jdbcTemplate.execute("EXEC sp_getapplock @Resource = ?, @LockTimeout='3600000'," +
                    " @LockMode = 'Exclusive', @LockOwner = 'Session'", lockName);
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to acquire SQL Server application lock", e);
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
                connection.setCurrentDatabase(databaseName);
                jdbcTemplate.execute("EXEC sp_releaseapplock @Resource = ?, @LockOwner = 'Session'", lockName);
            } catch (SQLException e) {
                LOG.error("Unable to release SQL Server application lock", e);
            }
        }
    }
}