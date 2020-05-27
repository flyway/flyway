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

public class ResourceNameCache {
    /**
     * Cache resource names.
     */
    private final Map<ClassPathLocationScanner, Map<URL, Set<String>>> resourceNameCache = new HashMap<>();

    public void put(ClassPathLocationScanner classPathLocationScanner, Map<URL, Set<String>> map){
        resourceNameCache.put(classPathLocationScanner, map);
    }

    public void put(ClassPathLocationScanner classPathLocationScanner, URL resolvedUrl, Set<String> names){
        resourceNameCache.get(classPathLocationScanner).put(resolvedUrl, names);
    }

    public Set<String> get(ClassPathLocationScanner classPathLocationScanner, URL resolvedUrl){
        return resourceNameCache.get(classPathLocationScanner).get(resolvedUrl);
    }
}