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
import org.flywaydb.core.api.callback.CallbackEvent;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.GenericCallback;
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
public class ScriptMigrationResolver<E extends CallbackEvent<E>> implements MigrationResolver {

    private final String[] fileTypes = new String[] {"cmd", "bat", "ps1", "py", "sh", "bash"};
    private final String[] suffixes = Arrays.stream(fileTypes).map(s -> "." + s).toArray(String[]::new);
    private final ResourceProvider resourceProvider;
    private final Configuration configuration;
    private final ParsingContext parsingContext;
    private final StatementInterceptor statementInterceptor;
    public final Set<GenericCallback<E>> scriptCallbacks;

    public ScriptMigrationResolver(final ResourceProvider resourceProvider, final Configuration configuration, final ParsingContext parsingContext, final StatementInterceptor statementInterceptor) {
        this.resourceProvider = resourceProvider;
        this.configuration = configuration;
        this.parsingContext = parsingContext;
        this.statementInterceptor = statementInterceptor;
        this.scriptCallbacks = new HashSet<>();
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations(final Context context) {
        final List<ResolvedMigration> migrations = new ArrayList<>();

        addMigrations(CoreMigrationType.SCRIPT, migrations, configuration.getSqlMigrationPrefix(), false);

        for (final MigrationResolver migrationResolver : context.configuration.getPluginRegister()
            .getInstancesOf(MigrationResolver.class)) {
            final String prefix = migrationResolver.getPrefix(context.configuration);
            if (prefix != null) {
                final MigrationType migrationType = migrationResolver.getDefaultMigrationType();
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

    private LoadableResource[] createPlaceholderReplacingLoadableResources(final List<LoadableResource> loadableResources) {
        final List<LoadableResource> list = new ArrayList<>();

        for (final LoadableResource loadableResource : loadableResources) {
            final LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
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

    private Integer getChecksumForLoadableResource(final boolean repeatable, final List<LoadableResource> loadableResources) {
        if (repeatable && configuration.isPlaceholderReplacement()) {
            return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(loadableResources));
        }

        return ChecksumCalculator.calculate(loadableResources.toArray(LoadableResource[]::new));
    }

    private Integer getEquivalentChecksumForLoadableResource(final boolean repeatable, final List<LoadableResource> loadableResources) {
        if (repeatable) {
            return ChecksumCalculator.calculate(loadableResources.toArray(LoadableResource[]::new));
        }

        return null;
    }

    private void addMigrations(final MigrationType migrationType, final List<ResolvedMigration> migrations, final String prefix, final boolean repeatable) {
        final ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (final LoadableResource resource : resourceProvider.getResources(prefix, new String[] { ""})) {
            final String filename = resource.getFilename();
            final ResourceName result = resourceNameParser.parse(filename, suffixes);

            if (!result.isValid() || isCallback(result) || !prefix.equals(result.getPrefix()) || isNotScriptFile(filename)) {
                continue;
            }

            final List<LoadableResource> resources = new ArrayList<>();
            resources.add(resource);

            final Integer checksum = getChecksumForLoadableResource(repeatable, resources);
            final Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);

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

    public void resolveCallbacks(final ParseCallbackEvent<E> parseCallbackEvent) {
        final ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (final LoadableResource resource : resourceProvider.getResources("", new String[] { ""})) {
            final String filename = resource.getFilename();
            final ResourceName result = resourceNameParser.parse(filename, suffixes);

            if (!result.isValid() || isNotScriptFile(filename)) {
                continue;
            }

            final Optional<E> maybeEvent = parseCallbackEvent.parse(result.getPrefix());
            if (maybeEvent.isPresent()) {
                LOG.debug("Found script callback: " + resource.getAbsolutePath() + " (filename: " + resource.getFilename() + ")");
                scriptCallbacks.add(new ArbitraryScriptCallback<>(
                        maybeEvent.get(),
                        result.getDescription(),
                        new ScriptMigrationExecutor(resource, parsingContext, result, statementInterceptor)
                ));
            }
        }
    }

    boolean isNotScriptFile(final String filename) {
        final boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
        final String extension = StringUtils.getFileNameAndExtension(filename).getRight();

        boolean isScriptFile = false;
        if (!isWindows) {
            isScriptFile = extension.isEmpty();
        }

        if (!isScriptFile) {
            for (final String suffix : fileTypes) {
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

    @FunctionalInterface
    public interface ParseCallbackEvent<E extends CallbackEvent<E>> {
        Optional<E> parse(final String id);
    }
}
