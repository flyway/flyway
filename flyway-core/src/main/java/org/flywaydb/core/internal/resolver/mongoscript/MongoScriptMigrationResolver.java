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
package org.flywaydb.core.internal.resolver.mongoscript;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.MongoScriptFlywayCallback;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Location;
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
 * Migration resolver for Mongo JavaScript files on the classpath. The javascript files must have
 * names like V1__Description.js or V1_1__Description.js.
 */
public class MongoScriptMigrationResolver implements MigrationResolver {

    /**
     * The database to use.
     */
    private final String databaseName;

    /**
     * The scanner to use.
     */
    private final Scanner scanner;

    /**
     * The base directory on the classpath where to migrations are located.
     */
    private final Location location;

    /**
     * The placeholder replacer to apply to mongo js migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The encoding of MongoScript migrations.
     */
    private final String encoding;

    /**
     * The prefix for MongoScript migrations
     */
    private final String mongoMigrationPrefix;

    /**
     * The prefix for repeatable MongoScript migrations
     */
    private final String mongoRepeatableMigrationPrefix;

    /**
     * The separator for MongoScript migrations
     */
    private final String mongoMigrationSeparator;

    /**
     * The suffix for MonoScript migrations
     */
    private final String mongoMigrationSuffix;

    /**
     * Creates a new instance.
     *
     * @param scanner              The Scanner for loading migrations on the classpath.
     * @param location             The location on the classpath where to migrations are located.
     * @param placeholderReplacer  The placeholder replacer to apply to mongo js migration scripts.
     * @param config               The flyway instance containing Mongo relevant information.
     */
    public MongoScriptMigrationResolver(Scanner scanner, Location location,
                                        PlaceholderReplacer placeholderReplacer,
                                        MongoFlywayConfiguration config) {
        this.scanner = scanner;
        this.location = location;
        this.placeholderReplacer = placeholderReplacer;
        this.encoding = config.getEncoding();
        this.databaseName = config.getDatabaseName();
        this.mongoMigrationPrefix = config.getMongoMigrationPrefix();
        this.mongoRepeatableMigrationPrefix = config.getRepeatableMongoMigrationPrefix();
        this.mongoMigrationSeparator = config.getMongoMigrationSeparator();
        this.mongoMigrationSuffix = config.getMongoMigrationSuffix();
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        scanForMigrations(migrations, mongoMigrationPrefix, mongoMigrationSeparator, mongoMigrationSuffix);
        scanForMigrations(migrations, mongoRepeatableMigrationPrefix, mongoMigrationSeparator, mongoMigrationSuffix);

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    public void scanForMigrations(List<ResolvedMigration> migrations, String prefix, String separator, String suffix) {
        for (Resource resource : scanner.scanForResources(location, prefix, suffix)) {
            String filename = resource.getFilename();
            if (isMongoScriptCallback(filename, suffix)) {
                continue;
            }
            Pair<MigrationVersion, String> info =
                    MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffix);

            ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
            migration.setVersion(info.getLeft());
            migration.setDescription(info.getRight());
            migration.setScript(extractScriptName(resource));
            migration.setChecksum(calculateChecksum(resource, resource.loadAsString(encoding)));
            migration.setType(MigrationType.MONGOSCRIPT);
            migration.setPhysicalLocation(resource.getLocationOnDisk());
            migration.setExecutor(new MongoScriptMigrationExecutor(resource, encoding, databaseName, placeholderReplacer));
            migrations.add(migration);
        }
    }

    /**
     * Checks whether this filename is actually a javascript-based callback instead of a regular migration.
     *
     * @param filename The filename to check.
     * @param suffix   The Mongo migration suffix.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isMongoScriptCallback(String filename, String suffix) {
        String baseName = filename.substring(0, filename.length() - suffix.length());
        return MongoScriptFlywayCallback.ALL_CALLBACKS.contains(baseName);
    }

    /**
     * Extracts the script name from this resource.
     *
     * @param resource The resource to process.
     * @return The script name.
     */
    /* private -> for testing */ String extractScriptName(Resource resource) {
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
