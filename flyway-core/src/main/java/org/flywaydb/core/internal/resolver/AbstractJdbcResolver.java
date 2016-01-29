package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.*;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractJdbcResolver<T> implements MigrationResolver, ConfigurationAware {

    protected FlywayConfiguration flywayConfiguration;
    protected Scanner scanner;
    protected List<ResolvedMigration> migrations;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }

    protected abstract Class<T> getTargetInterface();

    protected abstract MigrationExecutor createExecutor(T migration);

    protected abstract MigrationType getMigrationType();

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        migrations = new ArrayList<ResolvedMigration>();

        scanner = Scanner.create(flywayConfiguration.getClassLoader());
        for (Location location : new Locations(flywayConfiguration.getLocations()).getLocations()) {
            if (!location.isClassPath()) continue;
            resolveMigrationsFromSingleLocation(location);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());

        return migrations;
    }

    protected Collection<ResolvedMigration> resolveMigrationsFromSingleLocation(Location location) {
        try {
            Class<?>[] classes = scanner.scanForClasses(location, getTargetInterface());
            for (Class<?> clazz : classes) {
                T migration = ClassUtils.instantiate(clazz.getName(), flywayConfiguration.getClassLoader());
                ConfigurationInjectionUtils.injectFlywayConfiguration(migration, flywayConfiguration);

                ResolvedMigrationImpl migrationInfo = extractMigrationInfo(migration, location);
                migrationInfo.setPhysicalLocation(ClassUtils.getLocationOnDisk(clazz));
                migrationInfo.setExecutor(createExecutor(migration));

                migrations.add(migrationInfo);
            }
        } catch (Exception e) {
            throw new FlywayException("Unable to resolve Spring Jdbc Java migrations in location: " + location, e);
        }

        return migrations;
    }

    /**
     * Extracts the migration info from this migration.
     *
     * @param migration The migration to analyse.
     * @return The migration info.
     */
    protected ResolvedMigrationImpl extractMigrationInfo(T migration, Location location) {
        Integer checksum = null;
        if (migration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) migration;
            checksum = checksumProvider.getChecksum();
        }

        Pair<MigrationVersion, String> info = extractVersionAndDescription(migration, location);

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(info.getLeft());
        resolvedMigration.setDescription(info.getRight());
        resolvedMigration.setScript(migration.getClass().getName());
        resolvedMigration.setChecksum(checksum);
        resolvedMigration.setType(getMigrationType());
        return resolvedMigration;
    }

    protected Pair<MigrationVersion, String> extractVersionAndDescription(T migration, Location location) {
        Pair<MigrationVersion, String> info;
        if (migration instanceof MigrationInfoProvider) {
            return extractVersionAndDescriptionFromInterface((MigrationInfoProvider) migration);
        } else {
            info = extractVersionAndDescriptionFromClassName(migration, location);
        }
        return info;
    }

    protected Pair<MigrationVersion, String> extractVersionAndDescriptionFromClassName(T migration, Location location) {
        Pair<MigrationVersion, String> info;
        String shortName = ClassUtils.getShortName(migration.getClass());
        String prefix;
        if (shortName.startsWith("V") || shortName.startsWith("R")) {
            prefix = shortName.substring(0, 1);
        } else {
            throw new FlywayException("Invalid Jdbc migration class name: " + migration.getClass().getName()
                    + " => ensure it starts with V or R," +
                    " or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
        }
        info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "");
        return info;
    }

    protected Pair<MigrationVersion, String> extractVersionAndDescriptionFromInterface(MigrationInfoProvider migration) {
        MigrationVersion version;
        String description;
        Pair<MigrationVersion, String> info;
        MigrationInfoProvider infoProvider = migration;
        version = infoProvider.getVersion();
        description = infoProvider.getDescription();
        if (!StringUtils.hasText(description)) {
            throw new FlywayException("Missing description for migration " + version);
        }
        info = Pair.of(version, description);
        return info;
    }
}
