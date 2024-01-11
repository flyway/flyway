package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

public interface SqlScriptFactory {
    /**
     * @return A new SQL script.
     */
    SqlScript createSqlScript(LoadableResource resource, boolean mixed, ResourceProvider resourceProvider);
}