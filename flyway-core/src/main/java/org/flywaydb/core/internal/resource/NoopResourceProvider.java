package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

import java.util.Collection;
import java.util.Collections;

/**
 * No-op resource provider.
 */
public enum NoopResourceProvider implements ResourceProvider {
    INSTANCE;

    @Override
    public LoadableResource getResource(String name) {
        return null;
    }

    /**
     * Retrieve all resources whose name begins with this prefix and ends with any of these suffixes.
     *
     * @param prefix The prefix.
     * @param suffixes The suffixes.
     * @return The matching resources.
     */
    public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
        return Collections.emptyList();
    }
}