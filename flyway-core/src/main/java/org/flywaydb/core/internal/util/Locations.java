/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.Location;

/**
 * Encapsulation of a location list.
 */
@Getter
@CustomLog
public class Locations {
    private final List<Location> locations = new ArrayList<>();

    /**
     * @param rawLocations The raw locations to process.
     */
    public Locations(final String... rawLocations) {
        final List<Location> normalizedLocations = new ArrayList<>();
        for (final String rawLocation : rawLocations) {
            normalizedLocations.add(new Location(rawLocation));
        }
        processLocations(normalizedLocations);
    }

    /**
     * @param rawLocations The locations to process.
     */
    public Locations(final List<Location> rawLocations) {
        processLocations(rawLocations);
    }

    private void processLocations(final List<Location> rawLocations) {
        final List<Location> sortedLocations = new ArrayList<>(rawLocations);
        Collections.sort(sortedLocations);

        for (final Location normalizedLocation : sortedLocations) {
            if (locations.contains(normalizedLocation)) {
                LOG.warn("Discarding duplicate location '" + normalizedLocation + "'");
                continue;
            }

            final Location parentLocation = getParentLocationIfExists(normalizedLocation, locations);
            if (parentLocation != null) {
                LOG.warn("Discarding location '"
                    + normalizedLocation
                    + "' as it is a sub-location of '"
                    + parentLocation
                    + "'");
                continue;
            }

            locations.add(normalizedLocation);
        }
    }

    /**
     * Retrieves this location's parent within this list, if any.
     *
     * @param location       The location to check.
     * @param finalLocations The list to search.
     * @return The parent location. {@code null} if none.
     */
    private Location getParentLocationIfExists(final Location location, final Collection<Location> finalLocations) {
        return finalLocations.stream().filter(fl -> fl.isParentOf(location)).findFirst().orElse(null);
    }
}
