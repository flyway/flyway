/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration resolver for Java migrations. The classes must have a name like R__My_description, V1__Description
 * or V1_1_3__Description.
 */
public abstract class AbstractJavaMigrationResolver<M, E extends MigrationExecutor> implements MigrationResolver {
    private static final Log LOG = LogFactory.getLog(AbstractJavaMigrationResolver.class);

    /**
     * The Scanner to use.
     */
    private ClassProvider classProvider;

    /**
     * The configuration to inject (if necessary) in the migration classes.
     */
    private Configuration configuration;

    /**
     * Creates a new instance.
     *
     * @param classProvider The Scanner for loading migrations on the classpath.
     * @param configuration The configuration to inject (if necessary) in the migration classes.
     */
    public AbstractJavaMigrationResolver(ClassProvider classProvider, Configuration configuration) {
        this.classProvider = classProvider;
        this.configuration = configuration;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (Class<?> clazz : classProvider.getClasses(getImplementedInterface())) {
            M migration = ClassUtils.instantiate(clazz.getName(), configuration.getClassLoader());
            ConfigUtils.injectFlywayConfiguration(migration, configuration);

            ResolvedMigrationImpl migrationInfo = extractMigrationInfo(migration);
            migrationInfo.setPhysicalLocation(ClassUtils.getLocationOnDisk(clazz));
            migrationInfo.setExecutor(createExecutor(migration));

            migrations.add(migrationInfo);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    /**
     * @return The type of migration (for messages).
     */
    protected abstract String getMigrationTypeStr();

    /**
     * @return The interface the migrations must implement to be resolved.
     */
    protected abstract Class<M> getImplementedInterface();

    /**
     * Creates an executor for this migration.
     *
     * @param migration The migration.
     * @return The executor.
     */
    protected abstract E createExecutor(M migration);

    /**
     * Extracts the migration info from this migration.
     *
     * @param migration The migration to analyse.
     * @return The migration info.
     */
    public ResolvedMigrationImpl extractMigrationInfo(M migration) {
        Integer checksum = null;
        if (migration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) migration;
            checksum = checksumProvider.getChecksum();
        }

        MigrationVersion version;
        String description;



        if (migration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider) migration;
            version = infoProvider.getVersion();
            description = infoProvider.getDescription();
            if (!StringUtils.hasText(description)) {
                throw new FlywayException("Missing description for migration " + version);
            }











        } else {
            String shortName = ClassUtils.getShortName(migration.getClass());
            String prefix;



            boolean repeatable = shortName.startsWith("R");
            if (shortName.startsWith("V") || repeatable



            ) {
                prefix = shortName.substring(0, 1);
            } else {
                throw new FlywayException("Invalid " + getMigrationTypeStr() + " migration class name: " + migration.getClass().getName()
                        + " => ensure it starts with V" +



                        " or R," +
                        " or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
            }
            Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", new String[]{""}, repeatable);
            version = info.getLeft();
            description = info.getRight();
        }

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setScript(migration.getClass().getName());
        resolvedMigration.setChecksum(checksum);
        resolvedMigration.setType(getMigrationType(



        ));
        return resolvedMigration;
    }

    /**
     * The migration type to use.




     * @return The migration type.
     */
    protected abstract MigrationType getMigrationType(



    );
}