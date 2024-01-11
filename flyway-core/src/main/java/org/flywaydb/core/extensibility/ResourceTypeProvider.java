package org.flywaydb.core.extensibility;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.util.Pair;

import java.util.List;

public interface ResourceTypeProvider extends Plugin {
    List<Pair<String, ResourceType>> getPrefixTypePairs(Configuration configuration);
}