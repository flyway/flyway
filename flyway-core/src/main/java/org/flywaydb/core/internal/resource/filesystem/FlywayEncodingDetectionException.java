package org.flywaydb.core.internal.resource.filesystem;

import org.flywaydb.core.api.FlywayException;

public class FlywayEncodingDetectionException extends FlywayException {
    public FlywayEncodingDetectionException(String message) {
        super(message);
    }

    public FlywayEncodingDetectionException(String message, Exception cause) {
        super(message, cause);
    }
}