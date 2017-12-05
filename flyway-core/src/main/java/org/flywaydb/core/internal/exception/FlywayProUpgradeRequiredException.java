/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;

/**
 * Thrown when an attempt was made to use a Flyway Pro or Flyway Enterprise feature not supported by Flyway Open Source.
 */
public class FlywayProUpgradeRequiredException extends FlywayException {
    public FlywayProUpgradeRequiredException(String feature) {
        super("Flyway Pro or Flyway Enterprise upgrade required: " + feature
                + " is not supported by Flyway Open Source.");
    }
}
