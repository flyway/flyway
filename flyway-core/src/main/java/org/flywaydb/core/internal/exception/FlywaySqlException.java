package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * The specific exception thrown when Flyway encounters a problem in SQL statement.
 */
public class FlywaySqlException extends FlywayException {

    public FlywaySqlException(String message, SQLException sqlException) {
        super(message, sqlException, ErrorCode.DB_CONNECTION);
    }

    @Override
    public String getMessage() {
        String title = super.getMessage();
        String underline = StringUtils.trimOrPad("", title.length(), '-');

        return title + "\n" + underline + "\n" + ExceptionUtils.toMessage((SQLException) getCause());
    }
}