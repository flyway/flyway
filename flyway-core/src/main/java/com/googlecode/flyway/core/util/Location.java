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
package com.googlecode.flyway.core.util;

/**
 * A location to load migrations from.
 */
public class Location implements Comparable<Location> {
    /**
     * The location descriptor.
     */
    private String descriptor;

    /**
     * Creates a new location.
     *
     * @param descriptor The location descriptor.
     */
    public Location(String descriptor) {
        this.descriptor = normalizeDescriptor(descriptor);
    }

    /**
     * Normalizes this descriptor by
     * <ul>
     * <li>eliminating all leading and trailing spaces</li>
     * <li>eliminating all leading and trailing slashes</li>
     * <li>turning all separators into slashes</li>
     * </ul>
     *
     * @param descriptor The descriptor to normalize.
     * @return The normalized descriptor.
     */
    private String normalizeDescriptor(String descriptor) {
        String normalizedDescriptor = descriptor.trim().replace(".", "/").replace("\\", "/");
        if (normalizedDescriptor.startsWith("/")) {
            normalizedDescriptor = normalizedDescriptor.substring(1);
        }
        if (normalizedDescriptor.endsWith("/")) {
            normalizedDescriptor = normalizedDescriptor.substring(0, normalizedDescriptor.length() - 1);
        }
        return normalizedDescriptor;
    }

    /**
     * Checks whether this location is a parent of this other location.
     * @param other The other location.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isParentOf(Location other) {
        return (other.descriptor + "/").startsWith(descriptor + "/");
    }

    /**
     * @return The location descriptor.
     */
    public String getDescriptor() {
        return descriptor;
    }

    public int compareTo(Location o) {
        return descriptor.compareTo(o.descriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return descriptor.equals(location.descriptor);
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    @Override
    public String toString() {
        return descriptor;
    }
}
