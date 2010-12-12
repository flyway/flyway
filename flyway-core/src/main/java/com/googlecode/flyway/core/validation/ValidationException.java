package com.googlecode.flyway.core.validation;

import com.googlecode.flyway.core.exception.FlywayException;

/**
 * Exception indicating that the available migrations are inconsistent or incompatible with the applied migrations.
 */
public class ValidationException extends FlywayException {
    /**
     * Creates a new ValidationException with this validation message.
     *
     * @param message The validation message.
     */
    public ValidationException(String message) {
        super(message);
    }
}
