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

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */
public class Neo4JMigrationTest extends MigrationTestCase {

    private static final String JDBC_NEO4J_BOLT_URL = "jdbc:neo4j:bolt://localhost:7687";
    private static final String JDBC_NEO4J_USERNAME = "neo4j";
    private static final String JDBC_NEO4J_PASSWORD = "neo4j";

	protected static final String BASEDIR = "migration/dbsupport/neo4j/sql";

	protected static final String MIGRATIONDIR = "migration/dbsupport/neo4j";

	@Before
	public void purgeBeforeTest() throws SQLException {
	    dbSupport.getSchema(flyway.getSchemas()[0]).clean();
	}

	@Test
	public void getConnectionException() throws Exception {
		String url = "jdbc:neo4j:bolt:<<<Invalid--URL>>";
		String user = "foo";
		String password = "bar";

		try {
			new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null)
					.getConnection();
		} catch (FlywayException e) {
			assertTrue(e.getCause() instanceof SQLException);
			assertTrue(e.getMessage().contains(url));
			assertTrue(e.getMessage().contains(user));
			assertFalse(e.getMessage().contains(password));
		}
	}

	@Test
	public void nullInitSqls() throws Exception {
		new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, JDBC_NEO4J_BOLT_URL,
				JDBC_NEO4J_USERNAME, JDBC_NEO4J_PASSWORD, null).getConnection().close();
	}

	@Test
	public void migrateWithMixedMigrationsWorks() throws Exception {
	    assertNotNull(dataSource);
		assertNotNull(flyway);

		flyway.setLocations("migration/dbsupport/neo4j/sql");
		flyway.setMixed(true);

		assertTrue(flyway.migrate() == 4);
	}

    @Test
    public void cleanShouldDropConstraints() throws Exception {
        assertNotNull(dataSource);
        assertNotNull(flyway);

        jdbcTemplate.executeStatement("CREATE CONSTRAINT ON ( sample:SampleNode ) ASSERT sample.uuid IS UNIQUE");
        jdbcTemplate.executeStatement("CREATE CONSTRAINT ON ( other:OtherNode ) ASSERT other.uuid IS UNIQUE");

        assertEquals(2, jdbcTemplate.queryForStringList("CALL db.constraints").size());

        dbSupport.getSchema(flyway.getSchemas()[0]).clean();

        assertTrue(jdbcTemplate.queryForStringList("CALL db.constraints").isEmpty());
    }

    @Test
    public void migrateWithConstraintCreation() throws SQLException {
        assertNotNull(dataSource);
        assertNotNull(flyway);
        flyway.setLocations(getMigrationDir() + "/constraintcreation");

        assertEquals(2, flyway.migrate());
        assertEquals(2, jdbcTemplate.queryForStringList("CALL db.indexes").size());
    }

	@Override
	protected DataSource createDataSource(Properties customProperties) throws Exception {

		return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
		        JDBC_NEO4J_BOLT_URL, JDBC_NEO4J_USERNAME,
		        JDBC_NEO4J_PASSWORD, null);
	}

	@Override
	protected String getQuoteLocation() {
		return "migration/dbsupport/neo4j/quote/";
	}

	@Override
	public void upgradeMetadataTableTo40Format() throws Exception {
		createFlyway3MetadataTable();
		jdbcTemplate.execute("CREATE (t:test_user { name: " + dbSupport.quote("testUser1") + "} )");
		insertIntoFlyway3MetadataTable(jdbcTemplate, 1, 1, "0.1", "<< BASELINE >>", "BASELINE", "<< BASELINE >>", null,
				"flyway3", 0, true);
		insertIntoFlyway3MetadataTable(jdbcTemplate, 2, 2, "1", "First", "SQL", "V1__dummy.sql", -1883856720, "flyway3",
				15, true);
		flyway.setLocations(getBasedir());
		assertEquals(3, flyway.migrate());
		flyway.validate();
		assertEquals(5, flyway.info().applied().length);
		assertEquals(-1883856720, flyway.info().applied()[1].getChecksum().intValue());
	}

	private void insertIntoFlyway3MetadataTable(JdbcTemplate jdbcTemplate, int versionRank, int installedRank,
			String version, String description, String type, String script, Integer checksum, String installedBy,
			int executionTime, boolean success) throws SQLException {
		int checksumValue = checksum != null ? checksum : 0;
		jdbcTemplate.execute("MERGE (schemaVersion :schema_version);");
		jdbcTemplate.execute(" MATCH (sv :schema_version)  " + " CREATE (sv)-[:schema_versionToMigration]-> "
				+ " (Migration :Migration  " + "	{installed_rank:" + installedRank + "	,version: "
				+ dbSupport.quote(version) + "	,description:" + dbSupport.quote(description) + "	,type:"
				+ dbSupport.quote(type) + "	,script:" + dbSupport.quote(script) + "	,checksum:" + checksumValue
				+ "	,installed_by:" + dbSupport.quote(installedBy) + "	,installed_on:timestamp()"
				+ "	,execution_time:" + executionTime + "	,success:" + success + "});");
	}

	@Override
	protected String getBasedir() {
		return BASEDIR;
	}

	@Override
	protected String getMigrationDir() {
		return MIGRATIONDIR;
	}

	@Override
	protected void createTestTable() throws SQLException {
		jdbcTemplate.execute("CREATE (t:test_user { name: " + dbSupport.quote("testUser1") + "} )");
	}

	@Override
	protected String getFutureFailedLocation() {
		return getMigrationDir() + "/future_failed/";
	}

	@Override
	protected String getValidateLocation() {
		return getMigrationDir() + "/validate/";
	}

	@Override
	protected String getSemiColonLocation() {
		return getMigrationDir() + "/semicolon/";
	}

	@Override
	protected String getCommentLocation() {
		return getMigrationDir() + "/comment/";
	}

	@Override
	public void semicolonWithinStringLiteral() throws Exception {
		flyway.setLocations(getSemiColonLocation());
		flyway.migrate();

		assertEquals("1.1", flyway.info().current().getVersion().toString());
		assertEquals("Populate table", flyway.info().current().getDescription());

		assertEquals("Mr. Semicolon+Linebreak;\nanother line",
				jdbcTemplate.queryForString("MATCH (n:test_user) WHERE n.name CONTAINS 'line' RETURN n.name"));
	}

	@Override
	public void autoCommitFalse() {
		testAutoCommit(false);
	}

	@Override
	public void autoCommitTrue() {
		testAutoCommit(true);
	}

	private void testAutoCommit(boolean autoCommit) {
		DriverDataSource dataSource = (DriverDataSource) flyway.getDataSource();
		dataSource.setAutoCommit(autoCommit);
		flyway.setLocations(getBasedir());
		flyway.migrate();
		assertEquals("2.0", flyway.info().current().getVersion().getVersion());
	}

	@Override
	public void checkValidationWithInitRow() throws Exception {
		flyway.setLocations(getBasedir());
		flyway.setTarget(MigrationVersion.fromVersion("1.1"));
		flyway.migrate();
		assertEquals("1.1", flyway.info().current().getVersion().toString());

		jdbcTemplate.update("MATCH (n : " + flyway.getTable() + ") OPTIONAL MATCH (n)-[r]->(m) DELETE n,r,m");
		flyway.setBaselineVersion(MigrationVersion.fromVersion("1.1"));
		flyway.setBaselineDescription("initial version 1.1");
		flyway.baseline();

		flyway.setTarget(MigrationVersion.LATEST);
		flyway.migrate();
		assertEquals("2.0", flyway.info().current().getVersion().toString());
		flyway.validate();
	}

	@Override
	public void migrate() throws Exception {
		flyway.setLocations(getBasedir());
		flyway.migrate();
		MigrationVersion version = flyway.info().current().getVersion();
		assertEquals("2.0", version.toString());
		assertEquals(0, flyway.migrate());

		// We should have 5 rows if we have a schema creation marker as the first entry,
		// 4 otherwise
		if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
			assertEquals(5, flyway.info().applied().length);
		} else {
			assertEquals(4, flyway.info().applied().length);
		}

		for (MigrationInfo migrationInfo : flyway.info().applied()) {
			assertChecksum(migrationInfo);
		}

		assertEquals(2, jdbcTemplate.queryForInt("MATCH (:all_misters)-->(n) RETURN COUNT(n)"));
	}

	@Override
	public void subDir() {
		flyway.setLocations(getMigrationDir() + "/subdir");
		assertEquals(3, flyway.migrate());
	}

	@Override
	public void failedMigration() throws Exception {
		String tableName = "before_the_error";

		flyway.setLocations(getMigrationDir() + "/failed");
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("tableName", dbSupport.quote(tableName));
		flyway.setPlaceholders(placeholders);

		try {
			flyway.migrate();
			fail();
		} catch (DbMigrate.FlywayMigrateSqlException e) {
			System.out.println(e.getMessage());
			// root cause of exception must be defined, and it should be
			// FlywaySqlScriptException
			assertNotNull(e.getCause());
			assertTrue(e.getCause() instanceof SQLException);
			// and make sure the failed statement was properly recorded
			assertEquals("1", e.getMigration().getVersion().getVersion());
			assertEquals(24, e.getLineNumber());
			assertEquals("THIS IS NOT VALID CYPHER", e.getStatement());
		}

		MigrationInfo migration = flyway.info().current();
		assertEquals(dbSupport.supportsDdlTransactions(),
				!dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable(tableName).exists());
		if (dbSupport.supportsDdlTransactions()) {
			assertNull(migration);
		} else {
			MigrationVersion version = migration.getVersion();
			assertEquals("1", version.toString());
			assertEquals("Should Fail", migration.getDescription());
			assertEquals(MigrationState.FAILED, migration.getState());

			// With schema markers, we'll have 2 applied
			if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
				assertEquals(2, flyway.info().applied().length);
			} else {
				assertEquals(1, flyway.info().applied().length);
			}

		}
	}

	@Override
	public void comment() {
		flyway.setLocations(getCommentLocation());
		assertEquals(1, flyway.migrate());
	}

	@Override
	public void customTableName() throws Exception {
		flyway.setLocations(getBasedir());
		flyway.setTable("my_custom_table");
		flyway.migrate();
		int count = jdbcTemplate.queryForInt("MATCH (n:Migration) RETURN COUNT(n) ");

		// Same as 'migrate()', count is 5 when we have a schema creation marker
		if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
			assertEquals(5, count);
		} else {
			assertEquals(4, count);
		}
	}

	@Override
	public void repairChecksum() {
		flyway.setLocations(getCommentLocation());
		Integer commentChecksum = flyway.info().pending()[0].getChecksum();

		flyway.setLocations(getQuoteLocation());
		Integer quoteChecksum = flyway.info().pending()[0].getChecksum();

		assertNotEquals(commentChecksum, quoteChecksum);

		flyway.migrate();

		if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
			assertEquals(quoteChecksum, flyway.info().applied()[1].getChecksum());
		} else {
			assertEquals(quoteChecksum, flyway.info().applied()[0].getChecksum());
		}

		flyway.setLocations(getCommentLocation());
		flyway.repair();

		if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
			assertEquals(commentChecksum, flyway.info().applied()[1].getChecksum());
		} else {
			assertEquals(commentChecksum, flyway.info().applied()[0].getChecksum());
		}
	}

	@Override
	@Ignore("Ignoring this test because schema notion dosent exist on Neo4J")
	public void setCurrentSchema() throws Exception {
	}

	@Override
	@Test
	@Ignore("Ignoring this test because schema notion dosent exist on Neo4J")
	public void nonEmptySchema() throws Exception {
	}

	@Override
    @Test
	@Ignore("Ignoring this test because schema notion dosent exist on Neo4J")
	public void migrateMultipleSchemas() throws Exception {
	}

	@Override
	public void quote() throws Exception {
		flyway.setLocations(getQuoteLocation());
		flyway.migrate();
		assertEquals("0", jdbcTemplate.queryForString("MATCH (t : test_user) WHERE t.name CONTAINS "
				+ dbSupport.quote(flyway.getSchemas()[0]) + " RETURN COUNT(t)"));
	}

	@Override
	public void quotesAroundTableName() {
		flyway.setLocations(getQuoteLocation());
		flyway.migrate();

		// Clean script must also be able to properly deal with these reserved keywords
		// in table names.
		flyway.clean();
	}

	@Override
	public void columnExists() throws Exception {
		createTestTable();
		flyway.baseline();
		assertTrue(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).hasColumn("installed_rank"));
		assertFalse(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).hasColumn("dummy"));
	}

	@Override
	@Ignore("Ignoring this test because schema notion dosent exist on Neo4J")
	public void nonEmptySchemaWithInitOnMigrateHighVersion() throws Exception {
	}

	@Override
	@Ignore("Ignoring this test because schema notion dosent exist on Neo4J")
	public void nonEmptySchemaWithInitOnMigrate() throws Exception {
	}

	@Override
	public void repair() throws Exception {
		flyway.setLocations(getFutureFailedLocation());
		assertEquals(4, flyway.info().all().length);

		try {
			flyway.migrate();
			fail();
		} catch (FlywayException e) {
			e.printStackTrace();
		}

		assertEquals("3", flyway.info().current().getVersion().toString());
		assertEquals(MigrationState.FAILED, flyway.info().current().getState());

		flyway.repair();
		assertEquals("2.0", flyway.info().current().getVersion().toString());
		assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
	}

}
