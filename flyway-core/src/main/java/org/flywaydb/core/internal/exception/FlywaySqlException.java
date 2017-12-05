/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * This specific exception thrown when Flyway encounters a problem in SQL statement.
 */
public class FlywaySqlException extends FlywayException {
    /**
     * Creates new instance of FlywaySqlScriptException.
     *
     * @param sqlException Cause of the problem.
     */
    public FlywaySqlException(String message, SQLException sqlException) {
        super(message, sqlException);
    }

    @Override
    public String getMessage() {
        String title = super.getMessage();
        String underline = StringUtils.trimOrPad("", title.length(), '-');

        SQLException cause = (SQLException) getCause();
        while (cause.getNextException() != null) {
            cause = cause.getNextException();
        }

        String message = "\n" + title + "\n" + underline + "\n";
        message += "SQL State  : " + cause.getSQLState() + "\n";
        message += "Error Code : " + cause.getErrorCode() + "\n";
        if (cause.getMessage() != null) {
            message += "Message    : " + cause.getMessage().trim() + "\n";
        }

        return message;
    }
}
