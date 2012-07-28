/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.migration.sql;

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationExecutor;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.scanner.ClassPathScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Migration resolver for sql files on the classpath. The sql files must have names like V1.sql or V1_1.sql or
 * V1__Description.sql or V1_1__Description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * The base directory on the classpath where to migrations are located.
     */
    private final String location;

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
     * @param location            The location on the classpath where to migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of Sql migrations.
     * @param sqlMigrationPrefix  The prefix for sql migrations
     * @param sqlMigrationSuffix  The suffix for sql migrations
     */
    public SqlMigrationResolver(String location, PlaceholderReplacer placeholderReplacer, String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix) {
        this.location = location;
        this.placeholderReplacer = placeholderReplacer;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }


    public List<ExecutableMigration> resolveMigrations() {
        List<ExecutableMigration> migrations = new ArrayList<ExecutableMigration>();

        try {
            ClassPathResource[] resources =
                    new ClassPathScanner().scanForResources(location, sqlMigrationPrefix, sqlMigrationSuffix);

            for (ClassPathResource resource : resources) {
                MigrationInfo migrationInfo = extractMigrationInfo(resource);
                String physicalLocation = resource.getLocationOnDisk();
                MigrationExecutor migrationExecutor = new SqlMigrationExecutor(resource, placeholderReplacer, encoding);

                migrations.add(new ExecutableMigration(migrationInfo, physicalLocation, migrationExecutor));
            }
        } catch (IOException e) {
            throw new FlywayException("Unable to scan for SQL migrations in location: " + location, e);
        }

        return migrations;
    }

    /**
     * Extracts the migration info for this resource.
     *
     * @param resource The resource to analyse.
     * @return The migration info.
     */
    private MigrationInfo extractMigrationInfo(ClassPathResource resource) {
        final String versionString =
                extractVersionStringFromFileName(resource.getFilename(), sqlMigrationPrefix, sqlMigrationSuffix);
        String scriptName = resource.getLocation().substring(resource.getLocation().indexOf(location) + location.length() + "/".length());

        String sqlScriptSource = resource.loadAsString(encoding);
        int checksum = calculateChecksum(sqlScriptSource);

        return new MigrationInfo(
                MigrationInfoHelper.extractVersion(versionString),
                MigrationInfoHelper.extractDescription(versionString),
                scriptName,
                checksum,
                MigrationType.SQL,
                MigrationState.PENDING);
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
     * Calculates the checksum of this sql script.
     *
     * @param sql The sql to calculate the checksum for.
     * @return The crc-32 checksum of the script.
     */
    private int calculateChecksum(String sql) {
        final CRC32 crc32 = new CRC32();
        crc32.update(sql.getBytes());
        return (int) crc32.getValue();
    }
}
