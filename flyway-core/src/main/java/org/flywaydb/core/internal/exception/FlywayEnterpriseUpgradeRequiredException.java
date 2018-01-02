/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;

/**
 * Thrown when an attempt was made to migrate an older database version no longer enjoying regular support by its
 * vendor and no longer supported by Flyway Open Source and Flyway Pro.
 */
public class FlywayEnterpriseUpgradeRequiredException extends FlywayException {
    public FlywayEnterpriseUpgradeRequiredException(String vendor, String database, String version) {
        super("Flyway Enterprise or " + database + " upgrade required: " + database + " " + version
                + " is past regular support by " + vendor
                + " and no longer supported by Flyway Open Source or Pro, but still supported by Flyway Enterprise.");
    }
}
