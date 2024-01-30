package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

/**
 * Callback that does nothing.
 */
public enum NoopCallback implements Callback {
    INSTANCE;

    @Override
    public boolean supports(Event event, Context context) {
        return false;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
    }

    @Override
    public String getCallbackName() {
        return "NOOP";
    }
}