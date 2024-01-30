package org.flywaydb.core.internal.scanner.classpath;

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
     */
    URL toStandardJavaUrl(URL url);
}