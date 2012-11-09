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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.migration.java.JavaMigrationResolver;
import com.googlecode.flyway.core.migration.jdbc.JdbcMigrationResolver;
import com.googlecode.flyway.core.migration.spring.SpringJdbcMigrationResolver;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.util.FeatureDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Facility for retrieving and sorting the available migrations from the classpath through the various migration
 * resolvers.
 */
public class CompositeMigrationResolver implements MigrationResolver {
    /**
     * The locations where the migrations are located.
     */
    private final String[] locations;

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
     * A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
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
    private List<ResolvedMigration> availableMigrations;

    /**
     * Creates a new CompositeMigrationResolver.
     *
     * @param locations          The locations where migrations are located.
     * @param basePackage        The base package where the Java migrations are located.
     * @param baseDir            The base directory on the classpath where the Sql migrations are located.
     * @param encoding           The encoding of Sql migrations.
     * @param sqlMigrationPrefix The file name prefix for sql migrations.
     * @param sqlMigrationSuffix The file name suffix for sql migrations.
     * @param placeholders       A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * @param placeholderPrefix  The prefix of every placeholder.
     * @param placeholderSuffix  The suffix of every placeholder.
     */
    public CompositeMigrationResolver(String[] locations, String basePackage, String baseDir, String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix, Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix) {
        this.locations = locations;
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
     * @return The available migrations, sorted by version, oldest first. An empty list is returned when no migrations
     *         can be found.
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    public List<ResolvedMigration> resolveMigrations() {
        if (availableMigrations == null) {
            availableMigrations = doFindAvailableMigrations();
        }

        return availableMigrations;
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, oldest first. An empty list is returned when no migrations
     *         can be found.
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    private List<ResolvedMigration> doFindAvailableMigrations() throws FlywayException {
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();

        Set<String> mergedLocations = mergeLocations();

        for (String location : mergedLocations) {
            migrationResolvers.add(new SqlMigrationResolver(location, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
            migrationResolvers.add(new JdbcMigrationResolver(location));

            if (FeatureDetector.isSpringJdbcAvailable()) {
                migrationResolvers.add(new SpringJdbcMigrationResolver(location));
                migrationResolvers.add(new JavaMigrationResolver(location));
            }
        }

        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>(collectMigrations(migrationResolvers));
        Collections.sort(migrations);

        checkForIncompatibilities(migrations);

        return migrations;
    }

    /**
     * Merges the locations from all different sources into a single non-overlapping set.
     *
     * @return The merged locations set.
     */
    /* private -> for testing */ Set<String> mergeLocations() {
        //TODO: In Flyway 2.0, add warnings for duplicates and overlaps

        //Use set to remove duplicates
        Set<String> mergedLocations = new TreeSet<String>();

        //Add legacy locations
        mergedLocations.add(baseDir);
        mergedLocations.add(basePackage);

        //Add new locations
        mergedLocations.addAll(Arrays.asList(locations));

        //Remove overlap. Ex: db/migration, db/migration/oracle -> db/migration
        if (mergedLocations.size() > 1) {
            Iterator<String> iterator = mergedLocations.iterator();
            String location1 = iterator.next();
            String location2 = iterator.next();

            boolean first = true;

            while (first || iterator.hasNext()) {
                first = false;

                if (location2.startsWith(location1)) {
                    iterator.remove();
                } else {
                    location1 = location2;
                }

                if (iterator.hasNext()) {
                    location2 = iterator.next();
                }
            }
        }

        return mergedLocations;
    }

    /**
     * Collects all the migrations for all migration resolvers.
     *
     * @param migrationResolvers The migration resolvers to check.
     * @return All migrations.
     */
    /* private -> for testing */
    static Collection<ResolvedMigration> collectMigrations(Collection<MigrationResolver> migrationResolvers) {
        Set<ResolvedMigration> migrations = new HashSet<ResolvedMigration>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            migrations.addAll(migrationResolver.resolveMigrations());
        }
        return migrations;
    }

    /**
     * Checks for incompatible migrations.
     *
     * @param migrations The migrations to check.
     * @throws FlywayException when two different migration with the same version number are found.
     */
    /* private -> for testing */
    static void checkForIncompatibilities(List<ResolvedMigration> migrations) {
        // check for more than one migration with same version
        for (int i = 0; i < migrations.size() - 1; i++) {
            ResolvedMigration current = migrations.get(i);
            ResolvedMigration next = migrations.get(i + 1);
            if (current.compareTo(next) == 0) {
                throw new FlywayException(String.format("Found more than one migration with version '%s' (Offenders: %s '%s' and %s '%s')",
                        current.getVersion(),
                        current.getType(),
                        current.getPhysicalLocation(),
                        next.getType(),
                        next.getPhysicalLocation()));
            }
        }
    }
}
