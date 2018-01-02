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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
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
 * Migration resolver for SQL files on the classpath. The SQL files must have names like
 * V1__Description.sql, V1_1__Description.sql or R__description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * Database-specific support.
     */
    private final Database database;

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
     * @param database            The database-specific support.
     * @param scanner             The Scanner for loading migrations on the classpath.
     * @param locations           The locations on the classpath where to migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param configuration       The Flyway configuration.
     */
    public SqlMigrationResolver(Database database, Scanner scanner, Locations locations,
                                PlaceholderReplacer placeholderReplacer, FlywayConfiguration configuration) {
        this.database = database;
        this.scanner = scanner;
        this.locations = locations;
        this.placeholderReplacer = placeholderReplacer;
        this.configuration = configuration;
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<>();

        String separator = configuration.getSqlMigrationSeparator();
        String[] suffixes = configuration.getSqlMigrationSuffixes();
        for (Location location : locations.getLocations()) {
            scanForMigrations(location, migrations, configuration.getSqlMigrationPrefix(), separator, suffixes,
                    false



            );




            scanForMigrations(location, migrations, configuration.getRepeatableSqlMigrationPrefix(), separator, suffixes,
                    true



            );
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    private void scanForMigrations(Location location, List<ResolvedMigration> migrations, String prefix,
                                   String separator, String[] suffixes, boolean repeatable



    ) {
        for (LoadableResource resource : scanner.scanForResources(location, prefix, suffixes)) {
            String filename = resource.getFilename();
            if (isSqlCallback(filename, suffixes)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffixes, repeatable);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource, location));
            migration.setChecksum(calculateChecksum(resource, resource.loadAsString(configuration.getEncoding())));
            migration.setType(



                            MigrationType.SQL);
            migration.setPhysicalLocation(resource.getLocationOnDisk());
            migration.setExecutor(new SqlMigrationExecutor(database, resource, placeholderReplacer, configuration));
            migrations.add(migration);
        }
    }

    /**
     * Checks whether this filename is actually a sql-based callback instead of a regular migration.
     *
     * @param filename The filename to check.
     * @param suffixes The sql migration suffixes.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isSqlCallback(String filename, String... suffixes) {
        for (String suffix : suffixes) {
            String baseName = filename.substring(0, filename.length() - suffix.length());
            if (SqlScriptFlywayCallback.ALL_CALLBACKS.contains(baseName)) {
                return true;
            }

        }
        return false;
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