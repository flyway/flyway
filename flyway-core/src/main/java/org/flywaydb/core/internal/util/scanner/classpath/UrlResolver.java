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

/**
 * Resolves container-specific URLs into standard Java URLs.
 */
public interface UrlResolver {
    /**
     * Resolves this container-specific URL into standard Java URL.
     *
     * @param url The URL to resolve.
     * @return The matching standard Java URL.
     * @throws java.io.IOException when the scanning failed.
     */
    URL toStandardJavaUrl(URL url) throws IOException;
}
