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
package org.flywaydb.core.internal.dbsupport.db2zos;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@Category(DbCategory.DB2zOS.class)
public class DB2zOSMigrationMediumTest extends MigrationTestCase {

	private String query;

	@Override
	protected void configureFlyway() {
		super.configureFlyway();
		flyway.setTable("SCHEMA_VERSION");
		flyway.setSchemas("TESTADM");
		flyway.setBaselineOnMigrate(true);
		try {
			jdbcTemplate.update("SET CURRENT SQLID = 'TESTADM';");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected DataSource createDataSource(Properties customProperties) throws Exception {
		String user = customProperties.getProperty("db2.user", "TESTADMS");
		String password = customProperties.getProperty("db2.password", "password");
		String url = customProperties.getProperty("db2.url", "jdbc:db2://host:port/schemaname");

		return new DriverDataSource(Thread.currentThread().getContextClassLoader(),null, url, user, password);
	}



	@Override
	protected String getQuoteLocation() {
		return "migration/quote";
	}

	@Test
	public void init() throws Exception {

		query = "select count(*) from sysibm.systables where dbname = 'TESTADM'";

		flyway.baseline();
		int countTablesAfterInit = jdbcTemplate.queryForInt(query);
		assertEquals(1, countTablesAfterInit);
	}

	@Test
	public void migrateCRUD() throws Exception {

		query = "select count(*) from sysibm.systables where dbname = 'TESTADM'";

		flyway.setLocations("migration/dbsupport/db2zos/sql/crud");

		int countTablesBeforeMigration = jdbcTemplate.queryForInt(query);
		assertEquals(0, countTablesBeforeMigration);
		flyway.baseline();
		flyway.migrate();
		int countTablesAfterMigration = jdbcTemplate.queryForInt(query);
		assertEquals(2, countTablesAfterMigration);

		MigrationVersion version = flyway.info().current().getVersion();
		assertEquals("1.2", version.toString());
		assertEquals("UpdateTable", flyway.info().current().getDescription());
		assertEquals("Nils", jdbcTemplate.queryForString("select firstname from TESTADM.PERSON where lastname = 'Nilsen'"));

	}

	@Test
	public void sequence() throws Exception {
		flyway.setLocations("migration/dbsupport/db2zos/sql/sequence");
		flyway.baseline();
		flyway.migrate();

		MigrationVersion migrationVersion = flyway.info().current().getVersion();
		assertEquals("1.1", migrationVersion.toString());
		assertEquals("Sequence", flyway.info().current().getDescription());
		assertEquals(666, jdbcTemplate.queryForInt("SELECT NEXTVAL FOR TESTADM.BEAST_SEQ from sysibm.sysdummy1"));
	}

	@Test
	public void type() throws Exception {
		flyway.setLocations("migration/dbsupport/db2zos/sql/type");
		flyway.baseline();
		flyway.migrate();
	}

	@Test
	public void view() throws Exception {
		flyway.setLocations("migration/dbsupport/db2zos/sql/view");
		flyway.baseline();
		flyway.migrate();
	}

	@Test
	public void trigger() throws Exception {
		flyway.setLocations("migration/dbsupport/db2zos/sql/trigger");
		flyway.baseline();
		flyway.migrate();
	}

	@Test
	public void routines() throws Exception {
		flyway.setLocations("migration/dbsupport/db2zos/sql/routines");
		flyway.baseline();
		flyway.migrate();
	}


	@Test
	public void alias() throws Exception {
		flyway.setLocations("migration/dbsupport/db2zos/sql/alias");
		flyway.baseline();
		flyway.migrate();
	}


	/**
	 * Override schema test. db2 on zOS does not support "Create schema"
	 *
	 * @throws Exception
	 */
	@Override
	@Test(expected = UnsupportedOperationException.class)
	public void setCurrentSchema() throws Exception {
		Schema schema = dbSupport.getSchema("current_schema_test");
		try {
			schema.create();

			flyway.setSchemas("current_schema_test");
			flyway.clean();

			flyway.setLocations("migration/current_schema");
			Map<String, String> placeholders = new HashMap<String, String>();
			placeholders.put("schema1", dbSupport.quote("current_schema_test"));
			flyway.setPlaceholders(placeholders);
			flyway.migrate();
		} finally {
			schema.drop();
		}
	}

	@Override
	@Test(expected = UnsupportedOperationException.class)
	public void migrateMultipleSchemas() throws Exception {
		flyway.setSchemas("flyway_1", "flyway_2", "flyway_3");
		flyway.clean();

		flyway.setLocations("migration/multi");
		Map<String, String> placeholders = new HashMap<String, String>();
		placeholders.put("schema1", dbSupport.quote("flyway_1"));
		placeholders.put("schema2", dbSupport.quote("flyway_2"));
		placeholders.put("schema3", dbSupport.quote("flyway_3"));
		flyway.setPlaceholders(placeholders);
		flyway.migrate();
		assertEquals("2.0", flyway.info().current().getVersion().toString());
		assertEquals("Add foreign key", flyway.info().current().getDescription());
		assertEquals(0, flyway.migrate());

		assertEquals(4, flyway.info().applied().length);
		assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_1") + ".test_user1"));
		assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_2") + ".test_user2"));
		assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_3") + ".test_user3"));

		flyway.clean();
	}


	@Test
	public void nonEmptySchemaWithDisableInitCheck() throws Exception {
		jdbcTemplate.execute("CREATE TABLE t1 (\n" +
				"  name VARCHAR(25) NOT NULL,\n" +
				"  PRIMARY KEY(name))");

		flyway.setLocations(BASEDIR);
		flyway.setBaselineVersionAsString("0_1");
		flyway.setBaselineOnMigrate(false);
		flyway.baseline();
		flyway.migrate();
	}

    /**
     * Check validation with INIT row.
     */
    @Override
    @Test
    public void checkValidationWithInitRow() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.setTarget(MigrationVersion.fromVersion("1.1"));
        flyway.migrate();
        assertEquals("1.1", flyway.info().current().getVersion().toString());

        jdbcTemplate.update("DROP TABLE " + dbSupport.quote(flyway.getTable()));
        jdbcTemplate.update("DROP TABLESPACE " + dbSupport.quote(flyway.getSchemas()) + ".SDBVERS");
        flyway.setBaselineVersionAsString("1.1");
        flyway.setBaselineDescription("initial version 1.1");
        flyway.baseline();

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        flyway.validate();
    }


}
