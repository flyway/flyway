/**
 * Copyright 2010-2016 Boxfuse GmbH
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
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
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
public class SqlMigrationResolver implements MigrationResolver, ConfigurationAware {

    /**
     * The scanner to use.
     */
    private Scanner scanner;

    /**
     * The base directory on the classpath where to migrations are located.
     */
    private Locations locations;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private PlaceholderReplacer placeholderReplacer;

    /**
     * The encoding of Sql migrations.
     */
    private String encoding;

    /**
     * The prefix for sql migrations
     */
    private String sqlMigrationPrefix;

    /**
     * The prefix for repeatable sql migrations
     */
    private String repeatableSqlMigrationPrefix;

    /**
     * The separator for sql migrations
     */
    private String sqlMigrationSeparator;

    /**
     * The suffix for sql migrations
     */
    private String sqlMigrationSuffix;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration configuration) {
        this.scanner = Scanner.create(configuration.getClassLoader());
        this.locations = new Locations(configuration.getLocations());
        this.placeholderReplacer = createPlaceholderReplacer(configuration);
        this.encoding = configuration.getEncoding();
        this.sqlMigrationPrefix = configuration.getSqlMigrationPrefix();
        this.repeatableSqlMigrationPrefix = configuration.getRepeatableSqlMigrationPrefix();
        this.sqlMigrationSeparator = configuration.getSqlMigrationSeparator();
        this.sqlMigrationSuffix = configuration.getSqlMigrationSuffix();
    }

    /**
     * @return A new, fully configured, PlaceholderReplacer.
     */
    private PlaceholderReplacer createPlaceholderReplacer(FlywayConfiguration config) {
        if (config.isPlaceholderReplacement()) {
            return new PlaceholderReplacer(config.getPlaceholders(), config.getPlaceholderPrefix(), config.getPlaceholderSuffix());
        }
        return PlaceholderReplacer.NO_PLACEHOLDERS;
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        for (Location location : locations.getLocations()) {
            scanForMigrationsInSingleLocation(location, migrations, sqlMigrationPrefix, sqlMigrationSeparator, sqlMigrationSuffix);
            scanForMigrationsInSingleLocation(location, migrations, repeatableSqlMigrationPrefix, sqlMigrationSeparator, sqlMigrationSuffix);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    public void scanForMigrationsInSingleLocation(Location location, List<ResolvedMigration> migrations, String prefix, String separator, String suffix) {
        for (Resource resource : scanner.scanForResources(location, prefix, suffix)) {
            String filename = resource.getFilename();
            if (isSqlCallback(filename, suffix)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffix);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource, location));
            migration.setChecksum(calculateChecksum(resource, resource.loadAsString(encoding)));
            migration.setType(MigrationType.SQL);
            migration.setPhysicalLocation(resource.getLocationOnDisk());
            migration.setExecutor(new SqlMigrationExecutor(resource, placeholderReplacer, encoding));
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
