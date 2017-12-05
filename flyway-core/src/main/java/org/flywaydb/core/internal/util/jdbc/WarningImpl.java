/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.errorhandler.Warning;

public class WarningImpl implements Warning {
    private final int code;
    private final String state;
    private final String message;

    /**
     * An warning that occurred while executing a statement.
     * @param code The warning code.
     * @param state The warning state.
     * @param message The warning message.
     */
    public WarningImpl(int code, String state, String message) {
        this.code = code;
        this.state = state;
        this.message = message;
    }

    /**
     * @return The warning code.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return The warning state.
     */
    public String getState() {
        return state;
    }

    /**
     * @return The warning message.
     */
    public String getMessage() {
        return message;
    }
}
