package org.flywaydb.core.internal.jdbc;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@CustomLog
public class PlainExecutionTemplate implements ExecutionTemplate {
    private final boolean skipErrorLog;

    public PlainExecutionTemplate() {
        this.skipErrorLog = false;
    }

    public PlainExecutionTemplate(boolean skipErrorLog) {
        this.skipErrorLog = skipErrorLog;
    }

    @Override
    public <T> T execute(Callable<T> callback) {
        try {
            LOG.debug("Performing operation in non-transactional context.");
            return callback.call();
        } catch (Exception e) {
            if (!skipErrorLog) {
                LOG.error("Failed to execute operation in non-transactional context. Please restore backups and roll back database and code!");
            }

            if (e instanceof SQLException) {
                throw new FlywaySqlException("Failed to execute operation.", (SQLException) e);
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new FlywayException(e);
        }
    }
}