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
package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Scans for classpath resources in this location.
 */
public interface ClassPathLocationScanner {
    /**
     * Finds the resource names below this location on the classpath under this locationUrl.
     *
     * @param location    The system-independent location on the classpath.
     * @param locationUrl The system-specific physical location URL.
     * @return The system-independent names of the resources on the classpath.
     * @throws IOException when the scanning failed.
     */
    Set<String> findResourceNames(String location, URL locationUrl) throws IOException;
}
