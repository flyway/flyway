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











    @Override
    public int compareTo(LoadableResource o) {
        return getRelativePath().compareTo(o.getRelativePath());
    }
}