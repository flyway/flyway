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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.util.Assert;

/**
 * @author ricardo.silva
 *
 */
public class Neo4JMigrationTest extends MigrationTestCase {
	 
	protected static final String BASEDIR = "migration/dbsupport/neo4j/sql/";
	
	protected static final String MIGRATIONDIR = "migration/dbsupport/neo4j/";


	    
	@Test
	public void migrateWithMixedMigrationsWorks() throws Exception {
		Assert.notNull(dataSource);
		Assert.notNull(flyway);

		flyway.setLocations("migration/dbsupport/neo4j/sql");
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
		return "migration/dbsupport/neo4j/quote/";
	}


	@Override
	public void upgradeMetadataTableTo40Format() throws Exception {
		createFlyway3MetadataTable();
		jdbcTemplate.execute("CREATE (t:test_user { name: "+ dbSupport.quote("testUser1") +"} )");
		insertIntoFlyway3MetadataTable(jdbcTemplate, 1, 1, "0.1", "<< BASELINE >>", "BASELINE", "<< BASELINE >>", null, "flyway3",
				0, true);
		insertIntoFlyway3MetadataTable(jdbcTemplate, 2, 2, "1", "First", "SQL", "V1__dummy.sql", 1694047048, "flyway3", 15,
				true);
		flyway.setLocations(getBasedir());
		assertEquals(3, flyway.migrate());
		flyway.validate();
		assertEquals(5, flyway.info().applied().length);
		assertEquals(1694047048, flyway.info().applied()[1].getChecksum().intValue());
	}

	private void insertIntoFlyway3MetadataTable(JdbcTemplate jdbcTemplate, int versionRank, int installedRank,
			String version, String description, String type, String script, Integer checksum, String installedBy,
			int executionTime, boolean success) throws SQLException {
			
			int checksumValue = checksum != null ? checksum : 0; 
			jdbcTemplate.execute("MERGE (schemaVersion :schema_version);"); 
			jdbcTemplate.execute(" MATCH (sv :schema_version)  " + 
				" CREATE (sv)-[:schema_versionToMigration]-> " + 
				" (Migration :Migration  " + 
				"	{installed_rank:" + installedRank + 
				"	,version: " + dbSupport.quote(version) + 
				"	,description:"+ dbSupport.quote(description) + 
				"	,type:" + dbSupport.quote(type) + 
				"	,script:"+ dbSupport.quote(script) + 
				"	,checksum:"+ checksumValue  + 
				"	,installed_by:"+ dbSupport.quote(installedBy) + 
				"	,installed_on:" + dbSupport.quote(Timestamp.valueOf(LocalDateTime.now()).toString()) + 
				"	,execution_time:"+ executionTime + 
				"	,success:" + success +"});");
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
		return getMigrationDir() + "future_failed/";
	}

	@Override
	protected String getValidateLocation() {
		return getMigrationDir() + "validate/";
	}

	@Override
	protected String getSemiColonLocation() {
		return getMigrationDir() + "semicolon/";
	}

	@Override
	protected String getCommentLocation() {
		return getMigrationDir() + "comment/";
	}

	@Override
	public void semicolonWithinStringLiteral() throws Exception {
		flyway.setLocations(getSemiColonLocation());
		flyway.migrate();

		assertEquals("1.1", flyway.info().current().getVersion().toString());
		assertEquals("Populate table", flyway.info().current().getDescription());

		assertEquals("Mr. Semicolon+Linebreak;\nanother line",
				jdbcTemplate.queryForString("select name from test_user where name like '%line'"));
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

        jdbcTemplate.update("MATCH (n : " + flyway.getTable() + ") OPTIONAL MATCH (n)-[r]->(m) DELETE n,r");
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

        // We should have 5 rows if we have a schema creation marker as the first entry, 4 otherwise
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
	 

}
