package org.flywaydb.core.api.migration.baseline;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ResourceType;
import org.flywaydb.core.extensibility.ResourceTypeProvider;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BaselineResourceTypeProvider implements ResourceTypeProvider {
    private enum BaselineResourceType implements ResourceType {
        BASELINE_MIGRATION;

        @Override
        public boolean isVersioned() {
            return true;
        }
    }

    @Override
    public List<Pair<String, ResourceType>> getPrefixTypePairs(Configuration configuration) {
        List<Pair<String, ResourceType>> pairs = new ArrayList<>();
        pairs.add(Pair.of(configuration.getPluginRegister().getPlugin(BaselineMigrationConfigurationExtension.class).getBaselineMigrationPrefix(), BaselineResourceType.BASELINE_MIGRATION));
        return pairs;
    }
}