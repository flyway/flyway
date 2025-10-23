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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.locations.LocationParser;
import org.flywaydb.core.internal.scanner.ReadOnlyLocationHandler;

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
        processLocations(Arrays.stream(rawLocations).map(LocationParser::parseLocation).toList());
    }

    /**
     * @param rawLocations The locations to process.
     */
    public Locations(final List<Location> rawLocations) {
        processLocations(rawLocations);
    }

    private void processLocations(final List<Location> rawLocations) {
        final Collection<ReadOnlyLocationHandler> locationHandlers = Flyway.configure()
            .getPluginRegister()
            .getInstancesOf(ReadOnlyLocationHandler.class);

        final List<Location> sortedLocations = new ArrayList<>(rawLocations);
        Collections.sort(sortedLocations);

        for (final Location normalizedLocation : sortedLocations) {
            if (locations.contains(normalizedLocation)) {
                LOG.warn("Discarding duplicate location '" + normalizedLocation + "'");
                continue;
            }

            final Location parentLocation = getParentLocationIfExists(locationHandlers, normalizedLocation, locations);
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
    private Location getParentLocationIfExists(final Collection<? extends ReadOnlyLocationHandler> locationHandlers,
        final Location location,
        final Collection<Location> finalLocations) {
        return finalLocations.stream().filter(fl -> {
            final ReadOnlyLocationHandler locationHandler = locationHandlers.stream()
                .filter(x -> x.canHandleLocation(fl))
                .findFirst()
                .orElseThrow(() -> new FlywayException("Unknown prefix for location: " + fl.getPrefix()));
            return isParent(locationHandler, fl, location);
        }).findFirst().orElse(null);
    }

    private boolean isParent(final ReadOnlyLocationHandler locationHandler,
        final Location potentialParent,
        final Location location) {
        if (potentialParent.getPathRegex() != null || location.getPathRegex() != null) {
            return false;
        }

        final var pathSeparator = locationHandler.getPathSeparator();
        return (location.getDescriptor() + pathSeparator).startsWith(potentialParent.getDescriptor() + pathSeparator);
    }
}
