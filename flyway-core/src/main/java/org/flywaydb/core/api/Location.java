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
package org.flywaydb.core.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.flywaydb.core.internal.scanner.LocationParser;

/**
 * A location to load migrations from.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
     * The prefix for AWS S3 locations.
     */
    private static final String AWS_S3_PREFIX = "s3:";
    /**
     * The prefix for Google Cloud Storage locations.
     */
    private static final String GCS_PREFIX = "gcs:";

    /**
     * @return The prefix part of the location. Can be either classpath: or filesystem:.
     */
    @Getter
    private final String prefix;
    /**
     * The path part of the location.
     */
    private final String rawPath;
    /**
     * The first folder in the path. This will equal rawPath if the path does not contain any wildcards
     *
     * @return The root part of the path part of the location.
     */
    @Getter
    private final String rootPath;
    /**
     * @return The regex that matches wildcards in the original path. Null if the original path did not contain any
     * wildcards.
     */
    @Getter
    private final Pattern pathRegex;

    public static Location fromPath(final String prefix, final String path) {
        return new Location(prefix, path, path, null);
    }

    public static Location fromWildcardPath(final String prefix,
        final String rootPath,
        final String wildcardPath,
        final Pattern pathRegex) {
        return new Location(prefix, rootPath, wildcardPath, pathRegex);
    }

    public Location(final String descriptor) {
        final Location location = LocationParser.parseLocation(descriptor);
        this.rawPath = location.rawPath;
        this.rootPath = location.rootPath;
        this.prefix = location.prefix;
        this.pathRegex = location.pathRegex;
    }

    private Location(final String prefix, final String rootPath, final String rawPath, final Pattern pathRegex) {
        this.rawPath = rawPath;
        this.rootPath = rootPath;
        this.prefix = prefix;
        this.pathRegex = pathRegex;
    }

    /**
     * @return Whether the given path matches this locations regex. Will always return true when the location did not
     * contain any wildcards.
     */
    public boolean matchesPath(final String path) {
        if (pathRegex == null) {
            return true;
        }
        return pathRegex.matcher(path).matches();
    }

    /**
     * Returns the path relative to this location. If the location path contains wildcards, the returned path will be
     * relative to the last non-wildcard folder in the path.
     */
    public String getPathRelativeToThis(final String path) {
        if (pathRegex != null && pathRegex.pattern().contains("?<relpath>")) {
            final Matcher matcher = pathRegex.matcher(path);
            if (matcher.matches()) {
                final String relPath = matcher.group("relpath");
                if (relPath != null && !relPath.isEmpty()) {
                    return relPath;
                }
            }
        }

        return !rootPath.isEmpty() ? path.substring(rootPath.length() + 1) : path;
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
     * Checks whether this denotes a location in AWS S3.
     *
     * @return {@code true} if it does, {@code false} if it doesn't;
     */
    public boolean isAwsS3() {
        return AWS_S3_PREFIX.equals(prefix);
    }

    /**
     * Checks whether this denotes a location in Google cloud storage.
     *
     * @return {@code true} if it does, {@code false} if it doesn't;
     */
    public boolean isGCS() {
        return GCS_PREFIX.equals(prefix);
    }

    /**
     * Checks whether this location is a parent of this other location.
     *
     * @param other The other location.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isParentOf(final Location other) {
        if (pathRegex != null || other.pathRegex != null) {
            return false;
        }
        if (isClassPath() && other.isClassPath()) {
            return (other.getDescriptor() + "/").startsWith(getDescriptor() + "/");
        }
        if (isFileSystem() && other.isFileSystem()) {
            return (other.getDescriptor() + File.separator).startsWith(getDescriptor() + File.separator);
        }
        return false;
    }

    /**
     * @return The path part of the location.
     * @see #getRootPath()
     * @deprecated Use the root path instead. This path will not be a genuine path for wildcard locations, whereas the
     * root path is always a path.
     */
    @Deprecated
    public String getPath() {
        return rawPath;
    }

    /**
     * @return The complete location descriptor.
     */
    @EqualsAndHashCode.Include
    public String getDescriptor() {
        return prefix + rawPath;
    }

    @Override
    public int compareTo(final Location o) {
        return getDescriptor().compareTo(o.getDescriptor());
    }

    /**
     * @return The complete location descriptor.
     */
    @Override
    public String toString() {
        return getDescriptor();
    }
}
