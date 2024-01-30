package org.flywaydb.core.api.resource;

/**
 * A resource (such as a .sql file) used by Flyway.
 */
public interface Resource {
    /**
     * @return The absolute path and filename of the resource on the classpath or filesystem (path and filename).
     */
    String getAbsolutePath();

    /**
     * @return The absolute path and filename of this resource on disk, regardless of whether this resources
     * points at the classpath or filesystem.
     */
    String getAbsolutePathOnDisk();

    /**
     * @return The filename of this resource, without the path.
     */
    String getFilename();

    /**
     * @return The filename of this resource, as well as the path relative to the location where the resource was
     * loaded from.
     */
    String getRelativePath();
}