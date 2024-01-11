package org.flywaydb.core.api.exception;

import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

/**
 * Exception thrown when Flyway encounters a problem with Validate.
 */
public class FlywayValidateException extends FlywayException {

    public FlywayValidateException(ErrorDetails errorDetails, String allValidateMessages) {
        super("Validate failed: " + errorDetails.errorMessage + "\n" + allValidateMessages +
                      "\nNeed more flexibility with validation rules? Learn more: " + FlywayDbWebsiteLinks.CUSTOM_VALIDATE_RULES, errorDetails.errorCode);
    }

}