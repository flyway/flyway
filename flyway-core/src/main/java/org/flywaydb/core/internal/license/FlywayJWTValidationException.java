package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.ErrorCode;

public class FlywayJWTValidationException extends FlywayLicensingException {
    public FlywayJWTValidationException(String message) {
        super(message);
    }
    public FlywayJWTValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    public FlywayJWTValidationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
}