/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.scanner;

import org.flywaydb.core.internal.scanner.classpath.ClassPathLocationScanner;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocationScannerCache {

    /**
     * Cache the location scanner for each protocol.
     */
    private final Map<String, ClassPathLocationScanner> cache = new HashMap<>();

    public boolean containsKey(String protocol) {
        return cache.containsKey(protocol);
    }

    public ClassPathLocationScanner get(String protocol) {
        return cache.get(protocol);
    }

    public void put(String protocol, ClassPathLocationScanner scanner) {
        cache.put(protocol, scanner);
    }
}