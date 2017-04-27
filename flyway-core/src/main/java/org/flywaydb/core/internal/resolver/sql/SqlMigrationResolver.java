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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Migration resolver for sql files on the classpath. The sql files must have names like
 * V1__Description.sql or V1_1__Description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * Database-specific support.
     */
    private final DbSupport dbSupport;

    /**
     * The scanner to use.
     */
    private final Scanner scanner;

    /**
     * The base directories on the classpath where the migrations are located.
     */
    private final Locations locations;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The Flyway configuration.
     */
    private final FlywayConfiguration configuration;

    /**
     * Creates a new instance.
     *
     * @param dbSupport                    The database-specific support.
     * @param scanner                      The Scanner for loading migrations on the classpath.
     * @param locations                    The locations on the classpath where to migrations are located.
     * @param placeholderReplacer          The placeholder replacer to apply to sql migration scripts.
     * @param configuration                The Flyway configuration.
     */
    public SqlMigrationResolver(DbSupport dbSupport, Scanner scanner, Locations locations,
                                PlaceholderReplacer placeholderReplacer, FlywayConfiguration configuration) {
        this.dbSupport = dbSupport;
        this.scanner = scanner;
        this.locations = locations;
        this.placeholderReplacer = placeholderReplacer;
        this.configuration = configuration;
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        for (Location location: locations.getLocations()) {
            scanForMigrations(location, migrations, configuration.getSqlMigrationPrefix(), configuration.getSqlMigrationSeparator(), configuration.getSqlMigrationSuffix(), false);
            scanForMigrations(location, migrations, configuration.getRepeatableSqlMigrationPrefix(), configuration.getSqlMigrationSeparator(), configuration.getSqlMigrationSuffix(), true);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    private void scanForMigrations(Location location, List<ResolvedMigration> migrations, String prefix, String separator, String suffix, boolean repeatable) {
        for (Resource resource : scanner.scanForResources(location, prefix, suffix)) {
            String filename = resource.getFilename();
            if (isSqlCallback(filename, suffix)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffix, repeatable);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource, location));
            migration.setChecksum(calculateChecksum(resource, resource.loadAsString(configuration.getEncoding())));
            migration.setType(MigrationType.SQL);
            migration.setPhysicalLocation(resource.getLocationOnDisk());
            migration.setExecutor(new SqlMigrationExecutor(dbSupport, resource, placeholderReplacer, configuration));
            migrations.add(migration);
        }
    }

    /**
     * Checks whether this filename is actually a sql-based callback instead of a regular migration.
     *
     * @param filename The filename to check.
     * @param suffix   The sql migration suffix.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isSqlCallback(String filename, String suffix) {
        String baseName = filename.substring(0, filename.length() - suffix.length());
        return SqlScriptFlywayCallback.ALL_CALLBACKS.contains(baseName);
    }

    /**
     * Extracts the script name from this resource.
     *
     * @param resource The resource to process.
     * @return The script name.
     */
    /* private -> for testing */ String extractScriptName(Resource resource, Location location) {
        if (location.getPath().isEmpty()) {
            return resource.getLocation();
        }

        return resource.getLocation().substring(location.getPath().length() + 1);
    }

    /**
     * Calculates the checksum of this string.
     *
     * @param str The string to calculate the checksum for.
     * @return The crc-32 checksum of the bytes.
     */
    /* private -> for testing */
    static int calculateChecksum(Resource resource, String str) {
        final CRC32 crc32 = new CRC32();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                crc32.update(line.getBytes("UTF-8"));
            }
        } catch (IOException e) {
            String message = "Unable to calculate checksum";
            if (resource != null) {
                message += " for " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")";
            }
            throw new FlywayException(message, e);
        }

        return (int) crc32.getValue();
    }
}
