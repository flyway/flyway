package org.flywaydb.core.api;

import org.flywaydb.core.api.resource.LoadableResource;

import java.util.Collection;

/**
 * A facility to obtain loadable resources.
 */
public interface ResourceProvider {
    /**
     * Retrieves the resource with this name.
     *
     * @param name The name of the resource.
     * @return The resource or {@code null} if not found.
     */
    LoadableResource getResource(String name);

    /**
     * Retrieve all resources whose name begins with this prefix and ends with any of these suffixes.
     *
     * @param prefix The prefix.
     * @param suffixes The suffixes.
     * @return The matching resources.
     */
    Collection<LoadableResource> getResources(String prefix, String[] suffixes);
}