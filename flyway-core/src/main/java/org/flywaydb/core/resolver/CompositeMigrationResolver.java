/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.dbsupport.DbSupport;
import org.flywaydb.core.resolver.jdbc.JdbcMigrationResolver;
import org.flywaydb.core.resolver.spring.SpringJdbcMigrationResolver;
import org.flywaydb.core.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.util.FeatureDetector;
import org.flywaydb.core.util.Location;
import org.flywaydb.core.util.Locations;
import org.flywaydb.core.util.PlaceholderReplacer;

import java.util.ArrayList;
import java.util.Arrays;
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
     * The migration resolvers to use internally.
     */
    private Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();

    /**
     * The available migrations, sorted by version, newest first. An empty list is returned when no migrations can be
     * found.
     */
    private List<ResolvedMigration> availableMigrations;

    /**
     * Creates a new CompositeMigrationResolver.
     *
     * @param dbSupport                The database-specific support.
     * @param locations                The locations where migrations are located.
     * @param encoding                 The encoding of Sql migrations.
     * @param sqlMigrationPrefix       The file name prefix for sql migrations.
     * @param sqlMigrationSuffix       The file name suffix for sql migrations.
     * @param placeholders             A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * @param placeholderPrefix        The prefix of every placeholder.
     * @param placeholderSuffix        The suffix of every placeholder.
     * @param customMigrationResolvers Custom Migration Resolvers.
     */
    public CompositeMigrationResolver(DbSupport dbSupport, Locations locations,
                                      String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix,
                                      Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix,
                                      MigrationResolver... customMigrationResolvers) {
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        for (Location location : locations.getLocations()) {
            migrationResolvers.add(new SqlMigrationResolver(dbSupport, location, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
            migrationResolvers.add(new JdbcMigrationResolver(location));

            if (FeatureDetector.isSpringJdbcAvailable()) {
                migrationResolvers.add(new SpringJdbcMigrationResolver(location));
            }
        }

        migrationResolvers.addAll(Arrays.asList(customMigrationResolvers));
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, oldest first. An empty list is returned when no migrations
     * can be found.
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
     * can be found.
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    private List<ResolvedMigration> doFindAvailableMigrations() throws FlywayException {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>(collectMigrations(migrationResolvers));
        Collections.sort(migrations, new ResolvedMigrationComparator());

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
            if (current.getVersion().compareTo(next.getVersion()) == 0) {
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
