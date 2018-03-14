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

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<Location> locations;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The Flyway configuration.
     */
    private final Configuration configuration;

    /**
     * Creates a new instance.
     *
     * @param database            The database-specific support.
     * @param scanner             The Scanner for loading migrations on the classpath.
     * @param locations           The locations on the classpath where to migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param configuration       The Flyway configuration.
     */
    public SqlMigrationResolver(Database database, Scanner scanner, List<Location> locations,
                                PlaceholderReplacer placeholderReplacer, Configuration configuration) {
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
        for (Location location : locations) {
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
            if (isSqlCallback(filename, separator, suffixes)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffixes, repeatable);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource, location));
            migration.setChecksum(resource.checksum());
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
     * @param filename  The filename to check.
     * @param separator The separator to use.
     * @param suffixes  The sql migration suffixes.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isSqlCallback(String filename, String separator, String... suffixes) {
        for (String suffix : suffixes) {
            String baseName = filename.substring(0, filename.length() - suffix.length());
            int index = baseName.indexOf(separator);
            if (index >= 0) {
                baseName = baseName.substring(0, index);
            }
            if (Event.fromId(baseName) != null) {
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
}