package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ResourceType;
import org.flywaydb.core.extensibility.ResourceTypeProvider;
import org.flywaydb.core.internal.util.Pair;

import java.util.List;

public class UndoResourceTypeProvider implements ResourceTypeProvider {
    private enum UndoResourceType implements ResourceType {
        UNDO_MIGRATION;

        @Override
        public boolean isVersioned() {
            return true;
        }
    }

    @Override
    public List<Pair<String, ResourceType>> getPrefixTypePairs(Configuration configuration) {
        String prefix = configuration.getUndoSqlMigrationPrefix();
        return List.of(Pair.of(prefix, UndoResourceType.UNDO_MIGRATION));
    }
}
