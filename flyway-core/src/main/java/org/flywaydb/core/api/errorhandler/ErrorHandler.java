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

/**
 * Handler for warnings and errors that occur during a migration. This can be used to customize Flyway's behavior by for example
 * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 */
public interface ErrorHandler {
    /**
     * Handles warnings and errors that occurred during a migration.
     *
     * @param context The context.
     * @return {@code true} if they were handled, {@code false} if they weren't and Flyway should fall back to its default handling.
     */
    boolean handle(Context context);
}
