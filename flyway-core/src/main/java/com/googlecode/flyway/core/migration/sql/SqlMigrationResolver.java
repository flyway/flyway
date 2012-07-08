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

import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.scanner.ClassPathScanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Migration resolver for sql files on the classpath. The sql files must have names like V1.sql or V1_1.sql or
 * V1__Description.sql or V1_1__Description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SqlMigrationResolver.class);

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


    public List<Migration> resolveMigrations() {
        List<Migration> migrations = new ArrayList<Migration>();

        String normalizedBaseDir = location;
        if (normalizedBaseDir.startsWith("/")) {
            normalizedBaseDir = normalizedBaseDir.substring(1);
        }

        try {
            ClassPathResource[] resources =
                    new ClassPathScanner().scanForResources(normalizedBaseDir, sqlMigrationPrefix, sqlMigrationSuffix);

            String searchRoot = normalizedBaseDir + "/";
            for (ClassPathResource resource : resources) {
                final String versionString =
                        extractVersionStringFromFileName(resource.getFilename(), sqlMigrationPrefix, sqlMigrationSuffix);
                String location = resource.getLocation();
                String scriptName = location.substring(location.indexOf(searchRoot) + searchRoot.length());
                migrations.add(new SqlMigration(resource, placeholderReplacer, encoding, versionString, scriptName));
            }
        } catch (IOException e) {
            throw new FlywayException("Unable to scan for SQL migrations in location: " + location, e);
        }

        return migrations;
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
}
