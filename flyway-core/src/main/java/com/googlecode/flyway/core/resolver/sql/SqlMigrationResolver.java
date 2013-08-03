/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.resolver.sql;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.resolver.MigrationInfoHelper;
import com.googlecode.flyway.core.resolver.MigrationResolver;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.util.Location;
import com.googlecode.flyway.core.util.Pair;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import com.googlecode.flyway.core.util.Resource;
import com.googlecode.flyway.core.util.scanner.classpath.ClassPathScanner;
import com.googlecode.flyway.core.util.scanner.filesystem.FileSystemScanner;

import java.io.IOException;
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
     * The suffix for sql migrations
     */
    private final String sqlMigrationSuffix;

    /**
     * Creates a new instance.
     *
     * @param dbSupport           The database-specific support.
     * @param location            The location on the classpath where to migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of Sql migrations.
     * @param sqlMigrationPrefix  The prefix for sql migrations
     * @param sqlMigrationSuffix  The suffix for sql migrations
     */
    public SqlMigrationResolver(DbSupport dbSupport, Location location, PlaceholderReplacer placeholderReplacer, String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix) {
        this.dbSupport = dbSupport;
        this.location = location;
        this.placeholderReplacer = placeholderReplacer;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

        Resource[] resources = new Resource[0];
        try {
            if (location.isClassPath()) {
                resources =
                        new ClassPathScanner().scanForResources(location.getPath(), sqlMigrationPrefix, sqlMigrationSuffix);
            } else if (location.isFileSystem()) {
                resources =
                        new FileSystemScanner().scanForResources(location.getPath(), sqlMigrationPrefix, sqlMigrationSuffix);
            }

            for (Resource resource : resources) {
                ResolvedMigration resolvedMigration = extractMigrationInfo(resource);
                resolvedMigration.setPhysicalLocation(resource.getLocationOnDisk());
                resolvedMigration.setExecutor(new SqlMigrationExecutor(dbSupport, resource, placeholderReplacer, encoding));

                migrations.add(resolvedMigration);
            }
        } catch (IOException e) {
            throw new FlywayException("Unable to scan for SQL migrations in location: " + location, e);
        }

        Collections.sort(migrations);
        return migrations;
    }

    /**
     * Extracts the migration info for this resource.
     *
     * @param resource The resource to analyse.
     * @return The migration info.
     */
    private ResolvedMigration extractMigrationInfo(Resource resource) {
        ResolvedMigration migration = new ResolvedMigration();

        Pair<MigrationVersion, String> info =
                MigrationInfoHelper.extractVersionAndDescription(resource.getFilename(), sqlMigrationPrefix, sqlMigrationSuffix);
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
        int start = 0;

        if (location.getPath().length() > 0) {
            start = location.getPath().length() + "/".length();
        }

        return resource.getLocation().substring(start);
    }

    /**
     * Extracts the sql file version string from this file name.
     *
     * @param fileName The file name to parse.
     * @param prefix   The prefix to extract
     * @param suffix   The suffix to extract
     * @return The version string.
     */
    /* private -> for testing */
    static String extractVersionStringFromFileName(String fileName, String prefix, String suffix) {
        int lastDirSeparator = fileName.lastIndexOf("/");
        int extension = fileName.lastIndexOf(suffix);
        String withoutPathAndSuffix = fileName.substring(lastDirSeparator + 1, extension);
        if (withoutPathAndSuffix.startsWith(prefix)) {
            return withoutPathAndSuffix.substring(prefix.length());
        }
        return withoutPathAndSuffix;
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
