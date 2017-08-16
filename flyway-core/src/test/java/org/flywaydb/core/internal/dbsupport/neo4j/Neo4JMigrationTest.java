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
		Assert.notNull(dataSource);
		Assert.notNull(flyway);

		flyway.setLocations("migration/dbsupport/neo4j/");
		flyway.setMixed(true);

		Assert.isTrue(flyway.migrate() == 4);
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

}
