/*
 * Copyright 2010-2017 Boxfuse GmbH
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
/**
 * 
 */
package org.flywaydb.core.internal.dbsupport.neo4j;

import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;

import org.junit.Test;
import org.springframework.util.Assert;

/**
 * @author ricardo.silva
 *
 */
public class Neo4JMigrationTest extends MigrationTestCase {

	@Test
	public void migrateWithMixedMigrationsWorks() throws Exception {
		// System.out.println("_____________________________________________________________
		// OLA ________________________________________________________________");
		Assert.notNull(dataSource);
		Assert.notNull(flyway);

		flyway.setLocations("migration/dbsupport/neo4j/");
		flyway.setMixed(true);

		Assert.isTrue(flyway.migrate() == 1);
	}

	@Override
	protected DataSource createDataSource(Properties customProperties) throws Exception {
		String user = customProperties.getProperty("neo4j.user", "neo4j");
		String password = customProperties.getProperty("neo4j.password", "test");
		String url = customProperties.getProperty("neo4j.url", "jdbc:neo4j:bolt://localhost:7687/");
		return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
	}

	@Override
	protected String getQuoteLocation() {
		return "migration/quote";
	}

	protected String getFutureFailedLocation() {
		return "migration/dbsupport/neo4j/future_failed/";
	}

}
