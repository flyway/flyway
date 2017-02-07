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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.mongodb.MongoMigrationResolver;
import org.flywaydb.core.internal.resolver.spring.SpringMongoMigrationResolver;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Facility for retrieving and sorting the available migrations from the classpath through the various migration
 * resolvers.
 */
public class CompositeMongoMigrationResolver implements MigrationResolver {
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
	 * @param scanner                      The Scanner for loading migrations on the classpath.
	 * @param locations                    The locations where migrations are located.
	 * @param config                          The flyway instance containing relevant information.
	 * @param customMigrationResolvers     Custom Migration Resolvers.
	 */
	public CompositeMongoMigrationResolver(Scanner scanner, MongoFlywayConfiguration config, Locations locations,
																		MigrationResolver... customMigrationResolvers) {
		if (!config.isSkipDefaultResolvers()) {
			String databaseName = config.getDatabaseName();
			boolean springMongoAvailable = new FeatureDetector(scanner.getClassLoader()).isSpringMongoAvailable();

			for (Location location : locations.getLocations()) {
				migrationResolvers.add(new MongoMigrationResolver(scanner, location, config));
						
				if (springMongoAvailable) {
					migrationResolvers.add(new SpringMongoMigrationResolver(databaseName, scanner, location, config));
				}
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
			if (new ResolvedMigrationComparator().compare(current, next) == 0) {
				if (current.getVersion() != null) {
					throw new FlywayException(String.format("Found more than one migration with version %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
																									current.getVersion(),
																									current.getPhysicalLocation(),
																									current.getType(),
																									next.getPhysicalLocation(),
																									next.getType()));
				}
				throw new FlywayException(String.format("Found more than one repeatable migration with description %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
																								current.getDescription(),
																								current.getPhysicalLocation(),
																								current.getType(),
																								next.getPhysicalLocation(),
																								next.getType()));
			}
		}
	}

}
