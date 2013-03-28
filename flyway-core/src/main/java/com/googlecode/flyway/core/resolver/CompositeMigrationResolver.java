/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.resolver;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.resolver.java.JavaMigrationResolver;
import com.googlecode.flyway.core.resolver.jdbc.JdbcMigrationResolver;
import com.googlecode.flyway.core.resolver.spring.SpringJdbcMigrationResolver;
import com.googlecode.flyway.core.resolver.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.util.FeatureDetector;
import com.googlecode.flyway.core.util.Location;
import com.googlecode.flyway.core.util.Locations;
import com.googlecode.flyway.core.util.PlaceholderReplacer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facility for retrieving and sorting the available migrations from the classpath through the various migration
 * resolvers.
 */
public class CompositeMigrationResolver implements MigrationResolver {
    /**
     * The collection of MigrationResolvers to delegate to
     */
    private final Collection<MigrationResolver> migrationResolvers;

    /**
     * The available migrations, sorted by version, newest first. An empty list is returned when no migrations can be
     * found.
     */
    private List<ResolvedMigration> availableMigrations;

    /**
     * Creates a new RealCompositeMigrationResolver
     *
     * @param migrationResolvers The collection of MigrationResolvers to delegate to
     */
    public CompositeMigrationResolver(final Collection<MigrationResolver> migrationResolvers) {
        this.migrationResolvers = migrationResolvers;
    }

    /**
     * Factory method which creates the default CompositeMigrationResolver which supports SQL and Java migrations
     *
     * @param locations          The locations where migrations are located.
     * @param encoding           The encoding of Sql migrations.
     * @param sqlMigrationPrefix The file name prefix for sql migrations.
     * @param sqlMigrationSuffix The file name suffix for sql migrations.
     * @param placeholders       A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * @param placeholderPrefix  The prefix of every placeholder.
     * @param placeholderSuffix  The suffix of every placeholder.
     * @return The default CompositeMigrationResolver which supports SQL and Java migrations
     */
    public static CompositeMigrationResolver newDefaultInstance(Locations locations, String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix, Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix) {
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        Collection<MigrationResolver> defaultMigrationResolvers = new ArrayList<MigrationResolver>();

        for (Location location : locations.getLocations()) {
            defaultMigrationResolvers.add(new SqlMigrationResolver(location, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
            defaultMigrationResolvers.add(new JdbcMigrationResolver(location));

            if (FeatureDetector.isSpringJdbcAvailable()) {
                defaultMigrationResolvers.add(new SpringJdbcMigrationResolver(location));
                defaultMigrationResolvers.add(new JavaMigrationResolver(location));
            }
        }

        return new CompositeMigrationResolver(defaultMigrationResolvers);
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
     * Get all MigrationResolver being delegated to. This is useful for building a new CompositeMigrationResolver from
     * an existing one and a collection of additional MigrationResolvers.
     *
     * @return MigrationResolver being delegated to.
     */
    public Collection<MigrationResolver> getMigrationResolvers() {
        return migrationResolvers;
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, oldest first. An empty list is returned when no migrations
     *         can be found.
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    private List<ResolvedMigration> doFindAvailableMigrations() throws FlywayException {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>(collectMigrations(migrationResolvers));
        Collections.sort(migrations);

        checkForIncompatibilities(migrations);

        return migrations;
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
