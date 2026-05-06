package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.resource.UndoResourceTypeProvider;
import org.flywaydb.core.internal.sqlscript.SqlScript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UndoSqlMigrationResolver implements MigrationResolver {

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        Configuration config = context.configuration;
        ResourceNameParser parser = new ResourceNameParser(config);
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (LoadableResource resource : context.resourceProvider.getResources(
                UndoResourceTypeProvider.UNDO_PREFIX, config.getSqlMigrationSuffixes())) {
            ResourceName name = parser.parse(resource.getFilename());
            if (!name.isValid() || !UndoResourceTypeProvider.UNDO_PREFIX.equals(name.getPrefix())) {
                continue;
            }

            SqlScript sqlScript = context.sqlScriptFactory.createSqlScript(
                    resource, config.isMixed(), context.resourceProvider);
            Integer checksum = ChecksumCalculator.calculate(resource);

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
        return migrations;
    }
}
