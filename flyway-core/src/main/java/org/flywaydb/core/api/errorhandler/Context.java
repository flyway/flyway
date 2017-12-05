/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.api.errorhandler;

import java.util.List;

/**
 * The context passed to an error handler.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 */
public interface Context {
    /**
     * @return The warnings that were raised during a migration.
     */
    List<Warning> getWarnings();

    /**
     * @return The errors that were thrown during a migration.
     */
    List<Error> getErrors();
}
