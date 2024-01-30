package org.flywaydb.core.api.callback;

/**
 * Base implementation of Callback from which one can inherit. This is a convenience class that assumes by default that
 * all events are handled and all handlers can run within a transaction.
 */
public abstract class BaseCallback implements Callback {
    @Override
    public boolean supports(Event event, Context context) {
        return true;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public String getCallbackName() {
        String name = this.getClass().getSimpleName();
        if (name.contains("__")) {
            name = name.split("__")[1];
        }
        return name;
    }
}