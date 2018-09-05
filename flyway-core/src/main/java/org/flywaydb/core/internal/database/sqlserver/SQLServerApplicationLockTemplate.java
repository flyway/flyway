/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

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