/**
 * Copyright 2010-2015 Boxfuse GmbH
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

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

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
     * The base directory on the classpath where to migrations are located.
     */
    private final Location location;

    /**
     * The placeholder replacer to apply to sql migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * The encoding of Sql migrations.
     */
    private final String encoding;

    /**
     * The prefix for sql migrations
     */
    private final String sqlMigrationPrefix;

    /**
     * The separator for sql migrations
     */
    private final String sqlMigrationSeparator;

    /**
     * The suffix for sql migrations
     */
    private final String sqlMigrationSuffix;

    /**
     * Creates a new instance.
     *
     * @param dbSupport             The database-specific support.
     * @param classLoader           The ClassLoader for loading migrations on the classpath.
     * @param location              The location on the classpath where to migrations are located.
     * @param placeholderReplacer   The placeholder replacer to apply to sql migration scripts.
     * @param encoding              The encoding of Sql migrations.
     * @param sqlMigrationPrefix    The prefix for sql migrations
     * @param sqlMigrationSeparator The separator for sql migrations
     * @param sqlMigrationSuffix    The suffix for sql migrations
     */
    public SqlMigrationResolver(DbSupport dbSupport, ClassLoader classLoader, Location location,
                                PlaceholderReplacer placeholderReplacer, String encoding,
                                String sqlMigrationPrefix, String sqlMigrationSeparator, String sqlMigrationSuffix) {
        this.dbSupport = dbSupport;
        this.scanner = new Scanner(classLoader);
        this.location = location;
        this.placeholderReplacer = placeholderReplacer;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.sqlMigrationSeparator = sqlMigrationSeparator;
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        Resource[] resources = scanner.scanForResources(location, sqlMigrationPrefix, sqlMigrationSuffix);
        for (Resource resource : resources) {
            ResolvedMigrationImpl resolvedMigration = extractMigrationInfo(resource);
            resolvedMigration.setPhysicalLocation(resource.getLocationOnDisk());
            resolvedMigration.setExecutor(new SqlMigrationExecutor(dbSupport, resource, placeholderReplacer, encoding));

            migrations.add(resolvedMigration);
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    /**
     * Extracts the migration info for this resource.
     *
     * @param resource The resource to analyse.
     * @return The migration info.
     */
    private ResolvedMigrationImpl extractMigrationInfo(Resource resource) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();

        Pair<MigrationVersion, String> info =
                MigrationInfoHelper.extractVersionAndDescription(resource.getFilename(),
                        sqlMigrationPrefix, sqlMigrationSeparator, sqlMigrationSuffix);
        migration.setVersion(info.getLeft());
        migration.setDescription(info.getRight());

        migration.setScript(extractScriptName(resource));

        migration.setChecksum(calculateChecksum(resource.loadAsBytes()));
        migration.setType(MigrationType.SQL);
        return migration;
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
     * Calculates the checksum of these bytes.
     *
     * @param bytes The bytes to calculate the checksum for.
     * @return The crc-32 checksum of the bytes.
     */
    private static int calculateChecksum(byte[] bytes) {
        final CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return (int) crc32.getValue();
    }
}
