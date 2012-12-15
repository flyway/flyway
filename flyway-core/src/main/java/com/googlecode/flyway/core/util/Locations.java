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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encaupsulation of a location list.
 */
public class Locations {
    private static final Log LOG = LogFactory.getLog(Locations.class);

    /**
     * The backing list.
     */
    private final List<String> locations = new ArrayList<String>();

    /**
     * Creates a new Locations wrapper with these raw locations.
     *
     * @param rawLocations The raw locations to process.
     */
    public Locations(String... rawLocations) {
        List<String> normalizedLocations = new ArrayList<String>();
        for (String rawLocation : rawLocations) {
            normalizedLocations.add(normalizeLocation(rawLocation));
        }
        Collections.sort(normalizedLocations);

        for (String normalizedLocation : normalizedLocations) {
            if (locations.contains(normalizedLocation)) {
                LOG.warn("Discarding duplicate location '" + normalizedLocation + "'");
                continue;
            }

            String parentLocation = getParentLocationIfExists(normalizedLocation, locations);
            if (parentLocation != null) {
                LOG.warn("Discarding location '" + normalizedLocation + "' as it is a sublocation of '" + parentLocation + "'");
                continue;
            }

            locations.add(normalizedLocation);
        }
    }

    /**
     * @return The locations.
     */
    public List<String> getLocations() {
        return locations;
    }

    /**
     * Normalizes this classpath location by
     * <ul>
     * <li>eliminating all leading and trailing spaces</li>
     * <li>eliminating all leading and trailing slashes</li>
     * <li>turning all separators into slashes</li>
     * </ul>
     *
     * @param location The location to normalize.
     * @return The normalized location.
     */
    private String normalizeLocation(String location) {
        String directory = location.trim().replace(".", "/").replace("\\", "/");
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        if (directory.endsWith("/")) {
            directory = directory.substring(0, directory.length() - 1);
        }
        return directory;
    }

    /**
     * Retrieves this location's parent within this list, if any.
     *
     * @param location       The location to check.
     * @param finalLocations The list to search.
     * @return The parent location. {@code null} if none.
     */
    private String getParentLocationIfExists(String location, List<String> finalLocations) {
        for (String finalLocation : finalLocations) {
            if ((location + "/").startsWith(finalLocation + "/")) {
                return finalLocation;
            }
        }
        return null;
    }
}
