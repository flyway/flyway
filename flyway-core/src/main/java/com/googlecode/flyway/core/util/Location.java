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
