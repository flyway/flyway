/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util.scanner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for JarFileLocationScanner.
 */
public class JarFileLocationScannerSmallTest {
    @Test
    public void extractJarFileNameNoProtocol() throws Exception {
        JarFileLocationScanner scanner = new JarFileLocationScanner("zip");
        String jarFileName = scanner.extractJarFileName("C:/war/WEB-INF/lib/flyway-sample-1.6.2-SNAPSHOT.jar!/db/migration");
        assertEquals("C:/war/WEB-INF/lib/flyway-sample-1.6.2-SNAPSHOT.jar", jarFileName);
    }

    @Test
    public void extractJarFileNameExtraFileProtocol() throws Exception {
        JarFileLocationScanner scanner = new JarFileLocationScanner("zip");
        String jarFileName = scanner.extractJarFileName("file:/C:/flyway-sample-webapp-1.6.2-SNAPSHOT/WEB-INF/lib/flyway-sample-1.6.2-SNAPSHOT.jar!/db/migration");
        assertEquals("/C:/flyway-sample-webapp-1.6.2-SNAPSHOT/WEB-INF/lib/flyway-sample-1.6.2-SNAPSHOT.jar", jarFileName);
    }
}
