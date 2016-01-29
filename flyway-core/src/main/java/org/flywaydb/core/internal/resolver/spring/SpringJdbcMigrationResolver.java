/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.*;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration resolver for Spring Jdbc migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 * 
 * <p>This class an be replaced with a custom subclass. Note however that since this class is considered
 * internal API, such a subclass is tied to a specific version and my need to be updated when switching to
 * a new flyway version. In order to use a custom subclass:</p>
 * <ul>
 *     <li>create a subclass of this class</li>
 *     <li>disable the usage of the default resolvers using {@link org.flywaydb.core.Flyway#setSkipDefaultResolvers(boolean)}
 *     or the respective property in the flyway configuration file</li>
 *     <li>include the custom subclass as custom resolver using {@link org.flywaydb.core.Flyway#setResolvers(MigrationResolver...)},
 *     {@link org.flywaydb.core.Flyway#setResolversAsClassNames(String...)} or the respective property in the flyway configuration file</li>
 *     <li><b>if you replace this class with a subclass, and want to use the other default resolvers, you need
 *     to include them as custom resolvers as well!</b></li>
 * </ul>

 */
public class SpringJdbcMigrationResolver implements MigrationResolver, ConfigurationAware {

    /**
     * The Scanner to use.
     */
    protected Scanner scanner;

    /**
     * The configuration to inject (if necessary) in the migration classes.
     */
    protected FlywayConfiguration configuration;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration configuration) {
        this.configuration = configuration;
        this.scanner = Scanner.create(configuration.getClassLoader());
    }

    @Override
    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        for (Location location : new Locations(configuration.getLocations()).getLocations()) {
            if (!location.isClassPath()) {
                continue;
            }
            resolveMigrationsForSingleLocation(location, migrations);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    /**
     * Resolves the migrations for a single location. This can be overridden by subclasses.
     * @param location the location to scan.
     * @param migrations an existing List to add the results to.
     */
    protected void resolveMigrationsForSingleLocation(Location location, List<ResolvedMigration> migrations) {
        try {
            Class<?>[] classes = scanner.scanForClasses(location, SpringJdbcMigration.class);
            for (Class<?> clazz : classes) {
                SpringJdbcMigration springJdbcMigration = ClassUtils.instantiate(clazz.getName(), scanner.getClassLoader());
                ConfigurationInjectionUtils.injectFlywayConfiguration(springJdbcMigration, configuration);

                ResolvedMigrationImpl migrationInfo = extractMigrationInfo(springJdbcMigration);
                migrationInfo.setPhysicalLocation(ClassUtils.getLocationOnDisk(clazz));
                migrationInfo.setExecutor(new SpringJdbcMigrationExecutor(springJdbcMigration));

                migrations.add(migrationInfo);
            }
        } catch (Exception e) {
            throw new FlywayException("Unable to resolve Spring Jdbc Java migrations in location: " + location, e);
        }
    }

    /**
     * Extracts the migration info from this migration. This can be overridden to include custom mechanisms to
     * retrieve the meta data (for example from annotations or the package name).
     *
     * @param springJdbcMigration The migration to analyse.
     * @return The migration info.
     */
    protected ResolvedMigrationImpl extractMigrationInfo(SpringJdbcMigration springJdbcMigration) {
        Integer checksum = null;
        if (springJdbcMigration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) springJdbcMigration;
            checksum = checksumProvider.getChecksum();
        }

        MigrationVersion version;
        String description;
        if (springJdbcMigration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider) springJdbcMigration;
            version = infoProvider.getVersion();
            description = infoProvider.getDescription();
            if (!StringUtils.hasText(description)) {
                throw new FlywayException("Missing description for migration " + version);
            }
        } else {
            String shortName = ClassUtils.getShortName(springJdbcMigration.getClass());
            String prefix;
            if (shortName.startsWith("V") || shortName.startsWith("R")) {
                prefix = shortName.substring(0, 1);
            } else {
                throw new FlywayException("Invalid Jdbc migration class name: " + springJdbcMigration.getClass().getName()
                        + " => ensure it starts with V or R," +
                        " or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
            }
            Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "");
            version = info.getLeft();
            description = info.getRight();
        }

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setScript(springJdbcMigration.getClass().getName());
        resolvedMigration.setChecksum(checksum);
        resolvedMigration.setType(MigrationType.SPRING_JDBC);
        return resolvedMigration;
    }
}
