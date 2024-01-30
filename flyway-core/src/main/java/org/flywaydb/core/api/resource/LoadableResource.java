package org.flywaydb.core.api.resource;

import java.io.Reader;

/**
 * A loadable resource.
 */
public abstract class LoadableResource implements Resource, Comparable<LoadableResource> {
    /**
     * Reads the contents of this resource.
     *
     * @return The reader with the contents of the resource.
     */
    public abstract Reader read();

    /**
     * @return Whether it is recommended to stream this resource.
     */
    public boolean shouldStream() {
        return false;
    }

    @Override
    public int compareTo(LoadableResource o) {
        return getRelativePath().compareTo(o.getRelativePath());
    }
}