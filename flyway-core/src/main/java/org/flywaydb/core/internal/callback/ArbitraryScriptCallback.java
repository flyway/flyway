package org.flywaydb.core.internal.callback;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.resolver.script.ScriptMigrationExecutor;

import java.sql.Connection;
import java.sql.SQLException;

@CustomLog
@RequiredArgsConstructor
public class ArbitraryScriptCallback implements Callback, Comparable<ArbitraryScriptCallback> {

    private final Event event;
    private final String description;
    private final ScriptMigrationExecutor scriptMigrationExecutor;

    @Override
    public boolean supports(Event event, Context context) {
        return this.event == event;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return scriptMigrationExecutor.canExecuteInTransaction();
    }

    @Override
    public void handle(Event event, Context context) {
        LOG.info("Executing script callback: " + event.getId()
                         + (description == null ? "" : " - " + description)
                         + (scriptMigrationExecutor.canExecuteInTransaction() ? "" : " [non-transactional]"));

        try {
            scriptMigrationExecutor.execute(new org.flywaydb.core.api.executor.Context() {
                @Override
                public Configuration getConfiguration() {
                    return context.getConfiguration();
                }

                @Override
                public Connection getConnection() {
                    return context.getConnection();
                }
            });
        } catch (SQLException e) {
            LOG.error("Script callback \"" + description + "\" failed.", e);
        }
    }

    @Override
    public String getCallbackName() {
        return description;
    }

    @Override
    public int compareTo(ArbitraryScriptCallback o) {
        int result = event.compareTo(o.event);
        if (result == 0) {
            if (description == null) {
                return -1;
            }
            if (o.description == null) {
                return 1;
            }
            result = description.compareTo(o.description);
        }
        return result;
    }
}