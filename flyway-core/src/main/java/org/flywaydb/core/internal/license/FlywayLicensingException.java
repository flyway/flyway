package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;

public class FlywayLicensingException extends FlywayException {
    public FlywayLicensingException(String message) {
        super(message);
    }
    public FlywayLicensingException(String message, Throwable cause) {
        super(message, cause);
    }
    public FlywayLicensingException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
}