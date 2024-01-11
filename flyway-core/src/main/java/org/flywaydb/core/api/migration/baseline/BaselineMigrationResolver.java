package org.flywaydb.core.api.migration.baseline;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationExecutor;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.util.*;
import java.util.stream.Collectors;

@CustomLog
public class BaselineMigrationResolver implements MigrationResolver {

    protected static boolean isSqlCallback(ResourceName result) {
        return Event.fromId(result.getPrefix()) != null;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        BaselineMigrationConfigurationExtension configurationExtension = context.configuration.getPluginRegister().getPlugin(BaselineMigrationConfigurationExtension.class);
        Configuration configuration = context.configuration;

        addMigrations(migrations, configurationExtension.getBaselineMigrationPrefix(), configuration, context.resourceProvider, context.sqlScriptFactory, context.sqlScriptExecutorFactory);

        migrations.sort(new ResolvedMigrationComparator());
        return migrations;
    }

    @Override
    public MigrationType getDefaultMigrationType() {
        return BaselineMigrationType.SQL_BASELINE;
    }

    @Override
    public String getPrefix(Configuration configuration) {
        return configuration.getPluginRegister().getPlugin(BaselineMigrationConfigurationExtension.class).getBaselineMigrationPrefix();
    }

    private void addMigrations(List<ResolvedMigration> migrations,
                               String prefix,
                               Configuration configuration,
                               ResourceProvider resourceProvider,
                               SqlScriptFactory sqlScriptFactory,
                               SqlScriptExecutorFactory sqlScriptExecutorFactory) {
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (LoadableResource resource : resourceProvider.getResources(prefix, configuration.getSqlMigrationSuffixes())) {
            String filename = resource.getFilename();
            ResourceName result = resourceNameParser.parse(filename);
            if (!result.isValid() || isSqlCallback(result) || !prefix.equals(result.getPrefix())) {
                continue;
            }

            SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, configuration.isMixed(), resourceProvider);

            List<LoadableResource> resources = new ArrayList<>();
            resources.add(resource);
            SortedSet<LoadableResource> referencedResources = new TreeSet<>();
            for (SqlScript referencedSqlScript : sqlScript.getReferencedSqlScripts()) {
                referencedResources.add(referencedSqlScript.getResource());
            }
            if (!referencedResources.isEmpty()) {
                LOG.debug("Calculating checksum for '" + filename + "' using the following referenced scripts: " +
                                  referencedResources.stream().map(Resource::getFilename).collect(Collectors.joining(",")));
            }
            resources.addAll(referencedResources);

            Integer checksum = getChecksumForLoadableResource(resources);

            migrations.add(new BaselineResolvedMigration(
                    result.getVersion(),
                    result.getDescription(),
                    resource.getRelativePath(),
                    checksum,
                    null,
                    resource.getAbsolutePathOnDisk(),
                    new SqlMigrationExecutor(sqlScriptExecutorFactory, sqlScript, false, configuration.isBatch()),
                    configuration));
        }
    }

    private Integer getChecksumForLoadableResource(List<LoadableResource> loadableResources) {
        return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
    }
}