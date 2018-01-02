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
package org.flywaydb.core.internal.util.scanner;

/**
 * A resource (such as a .sql file) used by Flyway.
 */
public interface Resource {
    /**
     * @return The location of the resource on the classpath (path and filename).
     */
    String getLocation();

    /**
     * Retrieves the location of this resource on disk.
     *
     * @return The location of this resource on disk.
     */
    String getLocationOnDisk();

    /**
     * @return The filename of this resource, without the path.
     */
    String getFilename();
}
