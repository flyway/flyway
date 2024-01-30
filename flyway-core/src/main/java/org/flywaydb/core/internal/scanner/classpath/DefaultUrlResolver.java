package org.flywaydb.core.internal.scanner.classpath;

import java.net.URL;

/**
 * Default implementation of UrlResolver.
 */
public class DefaultUrlResolver implements UrlResolver {
    public URL toStandardJavaUrl(URL url) {
        return url;
    }
}