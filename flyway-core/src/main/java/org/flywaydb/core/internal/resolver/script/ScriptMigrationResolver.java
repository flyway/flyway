/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.resolver.script;

import lombok.CustomLog;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.callback.ArbitraryScriptCallback;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.Reader;
import java.util.*;

@CustomLog
public class ScriptMigrationResolver implements MigrationResolver {

    private final String[] fileTypes = new String[] {"cmd", "bat", "ps1", "py", "sh", "bash"};
    private final String[] suffixes = Arrays.stream(fileTypes).map(s -> "." + s).toArray(String[]::new);
    private final ResourceProvider resourceProvider;
    private final Configuration configuration;
    private final ParsingContext parsingContext;
    private final StatementInterceptor statementInterceptor;
    public final Set<Callback> scriptCallbacks;

    public ScriptMigrationResolver(ResourceProvider resourceProvider, Configuration configuration, ParsingContext parsingContext, StatementInterceptor statementInterceptor) {
        this.resourceProvider = resourceProvider;
        this.configuration = configuration;
        this.parsingContext = parsingContext;
        this.statementInterceptor = statementInterceptor;
        this.scriptCallbacks = new HashSet<>();
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        addMigrations(CoreMigrationType.SCRIPT, migrations, configuration.getSqlMigrationPrefix(), false);

        for (MigrationResolver migrationResolver : context.configuration.getPluginRegister().getPlugins(MigrationResolver.class)) {
            String prefix = migrationResolver.getPrefix(context.configuration);
            if (prefix != null) {
                MigrationType migrationType = migrationResolver.getDefaultMigrationType();
                if (migrationType == null) {
                    addMigrations(CoreMigrationType.SCRIPT, migrations, prefix, false);
                } else {
                    addMigrations(migrationType.isUndo() ? CoreMigrationType.UNDO_SCRIPT : migrationType, migrations, prefix, false);
                }
            }
        }

        addMigrations(CoreMigrationType.SCRIPT, migrations, configuration.getRepeatableSqlMigrationPrefix(), true);

        migrations.sort(new ResolvedMigrationComparator());
        return migrations;
    }

    private LoadableResource[] createPlaceholderReplacingLoadableResources(List<LoadableResource> loadableResources) {
        List<LoadableResource> list = new ArrayList<>();

        for (final LoadableResource loadableResource : loadableResources) {
            LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
                @Override
                public Reader read() {
                    return PlaceholderReplacingReader.createForScriptMigration(
                            configuration,
                            parsingContext,
                            loadableResource.read());
                }

                @Override
                public String getAbsolutePath() {return loadableResource.getAbsolutePath();}

                @Override
                public String getAbsolutePathOnDisk() {return loadableResource.getAbsolutePathOnDisk();}

                @Override
                public String getFilename() {return loadableResource.getFilename();}

                @Override
                public String getRelativePath() {return loadableResource.getRelativePath();}
            };

            list.add(placeholderReplacingLoadableResource);
        }

        return list.toArray(LoadableResource[]::new);
    }

    private Integer getChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
        if (repeatable && configuration.isPlaceholderReplacement()) {
            return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(loadableResources));
        }

        return ChecksumCalculator.calculate(loadableResources.toArray(LoadableResource[]::new));
    }

    private Integer getEquivalentChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
        if (repeatable) {
            return ChecksumCalculator.calculate(loadableResources.toArray(LoadableResource[]::new));
        }

        return null;
    }

    private void addMigrations(MigrationType migrationType, List<ResolvedMigration> migrations, String prefix, boolean repeatable) {
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (LoadableResource resource : resourceProvider.getResources(prefix, new String[] {""})) {
            String filename = resource.getFilename();
            ResourceName result = resourceNameParser.parse(filename, suffixes);

            if (!result.isValid() || isCallback(result) || !prefix.equals(result.getPrefix()) || isNotScriptFile(filename)) {
                continue;
            }

            List<LoadableResource> resources = new ArrayList<>();
            resources.add(resource);

            Integer checksum = getChecksumForLoadableResource(repeatable, resources);
            Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);

            migrations.add(new ResolvedMigrationImpl(
                    result.getVersion(),
                    result.getDescription(),
                    resource.getRelativePath(),
                    checksum,
                    equivalentChecksum,
                    migrationType,
                    resource.getAbsolutePathOnDisk(),
                    new ScriptMigrationExecutor(resource, parsingContext, result, statementInterceptor)));
        }
    }

    public void resolveCallbacks() {
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (LoadableResource resource : resourceProvider.getResources("", new String[] {""})) {
            String filename = resource.getFilename();
            ResourceName result = resourceNameParser.parse(filename, suffixes);

            if (!result.isValid() || isNotScriptFile(filename)) {
                continue;
            }

            if (isCallback(result)) {
                LOG.debug("Found script callback: " + resource.getAbsolutePath() + " (filename: " + resource.getFilename() + ")");
                scriptCallbacks.add(new ArbitraryScriptCallback(
                        Event.fromId(result.getPrefix()),
                        result.getDescription(),
                        new ScriptMigrationExecutor(resource, parsingContext, result, statementInterceptor)
                ));
            }
        }
    }

    boolean isNotScriptFile(String filename) {
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
        String extension = StringUtils.getFileNameAndExtension(filename).getRight();

        boolean isScriptFile = false;
        if (!isWindows) {
            isScriptFile = extension.isEmpty();
        }

        if (!isScriptFile) {
            for (String suffix : fileTypes) {
                if (suffix.equalsIgnoreCase(extension)) {
                    isScriptFile = true;
                    break;
                }
            }
        }

        return !isScriptFile;
    }

    /**
     * Checks whether this filename is actually a callback instead of a regular migration.
     *
     * @param result The parsing result to check.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    private static boolean isCallback(ResourceName result) {
        return Event.fromId(result.getPrefix()) != null;
    }
}
