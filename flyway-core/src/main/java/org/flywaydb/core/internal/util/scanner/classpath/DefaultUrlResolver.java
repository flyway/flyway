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
 * Default implementation of UrlResolver.
 */
public class DefaultUrlResolver implements UrlResolver {
    public URL toStandardJavaUrl(URL url) throws IOException {
        return url;
    }
}
