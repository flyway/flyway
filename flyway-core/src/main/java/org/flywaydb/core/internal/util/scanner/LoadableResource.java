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
 * A loadable resource.
 */
public interface LoadableResource extends Resource {

    /**
     * Loads this resource as a string.
     *
     * @param encoding The encoding to use.
     * @return The string contents of the resource.
     */
    String loadAsString(String encoding);

    /**
     * Loads this resource as a byte array.
     *
     * @return The contents of the resource.
     */
    byte[] loadAsBytes();
}
