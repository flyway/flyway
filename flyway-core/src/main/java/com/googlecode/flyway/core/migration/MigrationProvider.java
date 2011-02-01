/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.java.JavaMigrationResolver;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.validation.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Facility for retrieving the available migrations.
 */
public class MigrationProvider {
    /**
     * The base package where the Java migrations are located.
     */
    private final String basePackage;

    /**
     * The base directory on the classpath where the Sql migrations are located.
     */
    private final String baseDir;

    /**
     * The encoding of Sql migrations.
     */
    private final String encoding;

    /**
     * The file name prefix for sql migrations.
     */
    private final String sqlMigrationPrefix;

    /**
     * The file name suffix for sql migrations.
     */
    private final String sqlMigrationSuffix;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     */
    private final Map<String, String> placeholders;

    /**
     * The prefix of every placeholder.
     */
    private final String placeholderPrefix;

    /**
     * The suffix of every placeholder.
     */
    private final String placeholderSuffix;

    /**
     * The available migrations, sorted by version, newest first. An empty list is returned when no migrations can be
     * found.
     */
    private List<Migration> availableMigrations;

    /**
     * Creates a new MigrationProvider.
     *
     * @param basePackage        The base package where the Java migrations are located.
     * @param baseDir            The base directory on the classpath where the Sql migrations are located.
     * @param encoding           The encoding of Sql migrations.
     * @param sqlMigrationPrefix The file name prefix for sql migrations.
     * @param sqlMigrationSuffix The file name suffix for sql migrations.
     * @param placeholders       A map of <placeholder, replacementValue> to apply to sql migration scripts.
     * @param placeholderPrefix  The prefix of every placeholder.
     * @param placeholderSuffix  The suffix of every placeholder.
     */
    public MigrationProvider(String basePackage, String baseDir, String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix, Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix) {
        this.basePackage = basePackage;
        this.baseDir = baseDir;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.sqlMigrationSuffix = sqlMigrationSuffix;
        this.placeholders = placeholders;
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, newest first. An empty list is returned when no migrations
     *         can be found.
     *
     * @throws com.googlecode.flyway.core.exception.FlywayException
     *          when the available migrations have overlapping versions.
     */
    public List<Migration> findAvailableMigrations() throws FlywayException {
        if (availableMigrations == null) {
            availableMigrations = doFindAvailableMigrations();
        }

        return availableMigrations;
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, newest first. An empty list is returned when no migrations
     *         can be found.
     *
     * @throws com.googlecode.flyway.core.exception.FlywayException
     *          when the available migrations have overlapping versions.
     */
    private List<Migration> doFindAvailableMigrations() throws FlywayException {
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(new SqlMigrationResolver(baseDir, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
        migrationResolvers.add(new JavaMigrationResolver(basePackage));

        List<Migration> allMigrations = new ArrayList<Migration>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            allMigrations.addAll(migrationResolver.resolveMigrations());
        }

        if (allMigrations.isEmpty()) {
            return allMigrations;
        }

        Collections.sort(allMigrations);
        Collections.reverse(allMigrations);

        // check for more than one migration with same version
        for (int i = 0; i < allMigrations.size() - 1; i++) {
            Migration current = allMigrations.get(i);
            Migration next = allMigrations.get(i + 1);
            if (current.compareTo(next) == 0) {
                throw new ValidationException("Found more than one migration with version: " + current.getVersion());
            }
        }

        return allMigrations;
    }
}
