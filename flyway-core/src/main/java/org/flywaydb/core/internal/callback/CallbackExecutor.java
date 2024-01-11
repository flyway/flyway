package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.output.OperationResult;

import java.util.List;

/**
 * Executes the callbacks for a specific event.
 */
public interface CallbackExecutor {
    /**
     * Executes the callbacks for this event on the main connection, within a separate transaction per callback if possible.
     *
     * @param event The event to handle.
     */
    void onEvent(Event event);

    /**
     * Executes the callbacks for this event on the migration connection, within a separate transaction per callback if possible.
     *
     * @param event The event to handle.
     */
    void onMigrateOrUndoEvent(Event event);

    /**
     * Sets the current migration info.
     *
     * @param migrationInfo The current migration.
     */
    void setMigrationInfo(MigrationInfo migrationInfo);

    /**
     * Executes the callbacks for an "each" event within the same transaction (if any) as the main operation.
     *
     * @param event The event to handle.
     */
    void onEachMigrateOrUndoEvent(Event event);














    /**
     * Executes the callbacks for an operation finish event.
     *
     * @param event The event to handle.
     * @param operationResult The operation result.
     */
    void onOperationFinishEvent(Event event, OperationResult operationResult);
}