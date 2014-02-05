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
package com.googlecode.flyway.core.resolver;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.DbSupport;
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
     * Database-specific support.
     */
    private final DbSupport dbSupport;

    /**
     * The locations where the migrations are located.
     */
    private final Locations locations;

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
     * Flag for enforcing unmatched placeholder checking.
     */
    private final boolean placeholderStrict;

    /**
     * The available migrations, sorted by version, newest first. An empty list is returned when no migrations can be
     * found.
     */
    private List<ResolvedMigration> availableMigrations;

    /**
     * Creates a new CompositeMigrationResolver.
     *
     * @param dbSupport           The database-specific support.
     * @param locations          The locations where migrations are located.
     * @param encoding           The encoding of Sql migrations.
     * @param sqlMigrationPrefix The file name prefix for sql migrations.
     * @param sqlMigrationSuffix The file name suffix for sql migrations.
     * @param placeholders       A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * @param placeholderPrefix  The prefix of every placeholder.
     * @param placeholderSuffix  The suffix of every placeholder.
     * @param placeholderStrict  Enforces unmatched placeholder checking.
     */
    public CompositeMigrationResolver(DbSupport dbSupport, Locations locations, String encoding, String sqlMigrationPrefix, String sqlMigrationSuffix, Map<String, String> placeholders, String placeholderPrefix, String placeholderSuffix, boolean placeholderStrict) {
        this.dbSupport = dbSupport;
        this.locations = locations;
        this.encoding = encoding;
        this.sqlMigrationPrefix = sqlMigrationPrefix;
        this.sqlMigrationSuffix = sqlMigrationSuffix;
        this.placeholders = placeholders;
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        this.placeholderStrict = placeholderStrict;
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
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix, placeholderStrict);

        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();

        for (Location location : locations.getLocations()) {
            migrationResolvers.add(new SqlMigrationResolver(dbSupport, location, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
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
