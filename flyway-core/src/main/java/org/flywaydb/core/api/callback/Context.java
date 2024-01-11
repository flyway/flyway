package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;

import java.sql.Connection;

/**
 * The context relevant to an event.
 */
public interface Context {
    /**
     * @return The configuration currently in use.
     */
    Configuration getConfiguration();

    /**
     * @return The JDBC connection being used. Transaction are managed by Flyway.
     * When the context is passed to the {@link Callback#handle(Event, Context)} method, a transaction will already have
     * been started if required and will be automatically committed or rolled back afterwards.
     */
    Connection getConnection();

    /**
     * @return The info about the migration being handled. Only relevant for the BEFORE_EACH_* and AFTER_EACH_* events.
     * {@code null} in all other cases.
     */
    MigrationInfo getMigrationInfo();

    /**
     * @return The info about the statement being handled. Only relevant for the statement-level events.
     * {@code null} in all other cases.
     * <p><i>Flyway Teams only</i></p>
     */
    Statement getStatement();

    /**
     * @return The OperationResult object for the finished operation. Only relevant for the AFTER_*_OPERATION_FINISH events.
     */
    OperationResult getOperationResult();
}