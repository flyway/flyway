package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ResourceType;
import org.flywaydb.core.extensibility.ResourceTypeProvider;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class CoreResourceTypeProvider implements ResourceTypeProvider {
    private enum CoreResourceType implements ResourceType {
        MIGRATION,
        REPEATABLE_MIGRATION,
        CALLBACK;

        /**
         * Whether the given resource type represents a resource that is versioned.
         */
        public boolean isVersioned() {
            return this == CoreResourceType.MIGRATION;
        }
    }

    @Override
    public List<Pair<String, ResourceType>> getPrefixTypePairs(Configuration configuration) {
        List<Pair<String, ResourceType>> pairs = new ArrayList<>();
        pairs.add(Pair.of(configuration.getSqlMigrationPrefix(), CoreResourceType.MIGRATION));
        pairs.add(Pair.of(configuration.getRepeatableSqlMigrationPrefix(), CoreResourceType.REPEATABLE_MIGRATION));

        for (Event event : Event.values()) {
            pairs.add(Pair.of(event.getId(), CoreResourceType.CALLBACK));
        }

        return pairs;
    }
}