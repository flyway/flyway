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
package org.flywaydb.core.internal.resolver.spring;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.flywaydb.core.internal.resolver.spring.dummy.V2__MongoInterfaceBasedMigration;
	import org.flywaydb.core.internal.resolver.spring.dummy.Version4dot5;
import org.flywaydb.core.internal.resolver.MongoFlywayConfigurationForTests;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringMongoMigrationResolverSmallTest {
	private final Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader());
	private final MongoFlywayConfiguration config = MongoFlywayConfigurationForTests.create();

	@Test
	public void resolveMigrations() {
		SpringMongoMigrationResolver migrationResolver =
			new SpringMongoMigrationResolver(null, scanner, new Location("org/flywaydb/core/internal/resolver/spring/dummy"), config);
		Collection<ResolvedMigration> migrations = migrationResolver.resolveMigrations();

		assertEquals(2, migrations.size());

		List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

		assertEquals("2", migrationList.get(0).getVersion().toString());
		assertEquals("4.5", migrationList.get(1).getVersion().toString());

		assertEquals("MongoInterfaceBasedMigration", migrationList.get(0).getDescription());
		assertEquals("Four Dot Five", migrationList.get(1).getDescription());

		assertNull(migrationList.get(0).getChecksum());
		assertEquals(45, migrationList.get(1).getChecksum().intValue());
	}

	@Test
	public void conventionOverConfiguration() {
		SpringMongoMigrationResolver springMongoMigrationResolver = new SpringMongoMigrationResolver(null, scanner, null, null);
		ResolvedMigration migrationInfo = springMongoMigrationResolver.extractMigrationInfo(new V2__MongoInterfaceBasedMigration());
		assertEquals("2", migrationInfo.getVersion().toString());
		assertEquals("MongoInterfaceBasedMigration", migrationInfo.getDescription());
		assertNull(migrationInfo.getChecksum());
	}

	@Test
	public void explicitInfo() {
		SpringMongoMigrationResolver springMongoMigrationResolver = new SpringMongoMigrationResolver(null, scanner, null, null);
		ResolvedMigration migrationInfo = springMongoMigrationResolver.extractMigrationInfo(new Version4dot5());
		assertEquals("4.5", migrationInfo.getVersion().toString());
		assertEquals("Four Dot Five", migrationInfo.getDescription());
		assertEquals(45, migrationInfo.getChecksum().intValue());
	}	
}
