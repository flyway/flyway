/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api;

import java.io.File;

/**
 * A location to load migrations from.
 */
public final class Location implements Comparable<Location> {
    /**
     * The prefix for classpath locations.
     */
    private static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * The prefix for filesystem locations.
     */
    public static final String FILESYSTEM_PREFIX = "filesystem:";

    /**
     * The prefix part of the location. Can be either classpath: or filesystem:.
     */
    private final String prefix;

    /**
     * The path part of the location.
     */
    private String path;

    /**
     * Creates a new location.
     *
     * @param descriptor The location descriptor.
     */
    public Location(String descriptor) {
        String normalizedDescriptor = descriptor.trim();

        if (normalizedDescriptor.contains(":")) {
            prefix = normalizedDescriptor.substring(0, normalizedDescriptor.indexOf(":") + 1);
            path = normalizedDescriptor.substring(normalizedDescriptor.indexOf(":") + 1);
        } else {
            prefix = CLASSPATH_PREFIX;
            path = normalizedDescriptor;
        }

        if (isClassPath()) {
            path = path.replace(".", "/");
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        } else if (isFileSystem()) {
            path = new File(path).getPath();
        } else {
            throw new FlywayException("Unknown prefix for location (should be either filesystem: or classpath:): "
                    + normalizedDescriptor);
        }

        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
    }

    /**
     * Checks whether this denotes a location on the classpath.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public boolean isClassPath() {
        return CLASSPATH_PREFIX.equals(prefix);
    }

    /**
     * Checks whether this denotes a location on the filesystem.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public boolean isFileSystem() {
        return FILESYSTEM_PREFIX.equals(prefix);
    }

    /**
     * Checks whether this location is a parent of this other location.
     *
     * @param other The other location.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isParentOf(Location other) {
        if (isClassPath() && other.isClassPath()) {
            return (other.getDescriptor() + "/").startsWith(getDescriptor() + "/");
        }
        if (isFileSystem() && other.isFileSystem()) {
            return (other.getPath() + File.separator).startsWith(getDescriptor() + File.separator);
        }
        return false;
    }

    /**
     * @return The prefix part of the location. Can be either classpath: or filesystem:.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return The path part of the location.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return The complete location descriptor.
     */
    public String getDescriptor() {
        return prefix + path;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(Location o) {
        return getDescriptor().compareTo(o.getDescriptor());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return getDescriptor().equals(location.getDescriptor());
    }

    @Override
    public int hashCode() {
        return getDescriptor().hashCode();
    }

    /**
     * @return The complete location descriptor.
     */
    @Override
    public String toString() {
        return getDescriptor();
    }
}