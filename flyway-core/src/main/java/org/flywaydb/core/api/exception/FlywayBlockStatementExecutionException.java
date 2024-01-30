package org.flywaydb.core.api.exception;

import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

/**
 * Exception thrown when callback wishes to block execution of SQL Statement
 */
public class FlywayBlockStatementExecutionException extends FlywayException {

    public FlywayBlockStatementExecutionException(ErrorDetails errorDetails, String blockReason) {
        super("Execution blocked: " + errorDetails.errorMessage + "\n" + blockReason, errorDetails.errorCode);
    }

}