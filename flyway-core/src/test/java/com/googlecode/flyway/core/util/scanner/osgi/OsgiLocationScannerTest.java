/**
 * Copyright (C) 2010-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.util.scanner.osgi;

import com.googlecode.flyway.core.util.scanner.LocationScanner;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for OsgiLocationScanner.
 */
public class OsgiLocationScannerTest {

    @Test
    public void findResourceNamesRemovesLeadingSlash() throws Exception {
        URL migration = createURL("bundle", "379.2", 0, "/db/migration/V1__Initial_version.sql");
        Enumeration<URL> urls = createEnumeration(migration);

        Bundle bundle = mock(Bundle.class);
        when(bundle.findEntries("/db/migration", "*", true)).thenReturn(urls);

        LocationScanner scanner = createLocationScanner(bundle);

        String location = "/db/migration";
        URL locationUrl = createURL("bundle", "379.2", 0, location);
        Set<String> resourceNames = scanner.findResourceNames(location, locationUrl);

        assertEquals(1, resourceNames.size());
        assertEquals("db/migration/V1__Initial_version.sql", resourceNames.toArray()[0]);
    }

    @Test
    public void findResourceNamesDoesNotFailWithNonExistentPath() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.findEntries("/db/migration", "*", true)).thenReturn(null);

        LocationScanner scanner = createLocationScanner(bundle);

        String location = "/db/migration";
        URL locationUrl = createURL("bundle", "379.2", 0, location);
        Set<String> resourceNames = scanner.findResourceNames(location, locationUrl);

        assertEquals(0, resourceNames.size());
    }

    private static Enumeration<URL> createEnumeration(URL... urls) throws MalformedURLException {
        List<URL> values = new ArrayList<URL>();
        Collections.addAll(values, urls);

        return Collections.enumeration(values);
    }

    private static URL createURL(String protocol, String host, int port, String file) throws MalformedURLException {
        return new URL(protocol, host, port, file, new MockURLStreamHandler());
    }

    private static LocationScanner createLocationScanner(Bundle bundle) {
        BundleProvider provider = mock(BundleProvider.class);
        when(provider.getBundle()).thenReturn(bundle);

        return new OsgiLocationScanner(provider);
    }

}
