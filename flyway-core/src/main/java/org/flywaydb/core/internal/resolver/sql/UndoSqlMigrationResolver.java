package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UndoSqlMigrationResolver implements MigrationResolver {

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        Configuration config = context.configuration;
        String undoPrefix = config.getUndoSqlMigrationPrefix();
        ResourceNameParser parser = new ResourceNameParser(config);
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (LoadableResource resource : context.resourceProvider.getResources(
                undoPrefix, config.getSqlMigrationSuffixes())) {
            ResourceName name = parser.parse(resource.getFilename());
            if (!name.isValid() || !undoPrefix.equals(name.getPrefix())) {
                continue;
            }

            SqlScript sqlScript = context.sqlScriptFactory.createSqlScript(
                    resource, config.isMixed(), context.resourceProvider);
            Integer checksum = ChecksumCalculator.calculate(resource);

            // equivalentChecksum is null for versioned (non-repeatable) migrations — consistent with SqlMigrationResolver
            migrations.add(new ResolvedMigrationImpl(
                    name.getVersion(),
                    name.getDescription(),
                    resource.getRelativePath(),
                    checksum,
                    null,
                    CoreMigrationType.UNDO_SCRIPT,
                    resource.getAbsolutePathOnDisk(),
                    new SqlMigrationExecutor(context.sqlScriptExecutorFactory, sqlScript,
                            false, config.isBatch())));
        }

        migrations.sort(new ResolvedMigrationComparator());
        return migrations;
    }
}
