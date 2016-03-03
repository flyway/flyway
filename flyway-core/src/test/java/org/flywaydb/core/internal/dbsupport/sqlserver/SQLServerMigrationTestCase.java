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
package org.flywaydb.core.internal.dbsupport.sqlserver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test to demonstrate the migration functionality using SQL Server.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class SQLServerMigrationTestCase extends MigrationTestCase {
    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations("migration/failed");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", dbSupport.quote(tableName));
        flyway.setPlaceholders(placeholders);

        try {
            flyway.migrate();
            fail();
        } catch (FlywaySqlScriptException e) {
            // root cause of exception must be defined, and it should be FlywaySqlScriptException
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof SQLException);
            // and make sure the failed statement was properly recorded
            // Normal DB should fail at line 21. SqlServer fails at line 17 as statements are executed in batches.
            assertEquals(17, e.getLineNumber());
            assertTrue(e.getStatement().contains("THIS IS NOT VALID SQL"));
        }
    }

    /**
     * Tests clean and migrate for SQL Server Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForString("SELECT value FROM test_data"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server Functions.
     */
    @Test
    public void function() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/function");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server Triggers.
     */
    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/trigger");
        flyway.migrate();

        assertEquals(3, jdbcTemplate.queryForInt("SELECT priority FROM customers where name='MS Internet Explorer Team'"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server Types.
     */
    @Test
    public void type() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/type");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for SQL Server unicode strings.
     */
    @Test
    public void nvarchar() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/nvarchar");
        flyway.migrate();

        flyway.clean();
    }

    /**
     * Tests clean and migrate for SQL Server sequences.
     */
    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/sequence");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for default constraints with functions.
     */
    @Test
    public void defaultConstraints() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/default");
        flyway.migrate();

        flyway.clean();
    }

    /**
     * Tests migrate error for pk constraints.
     */
    @Test(expected = FlywayException.class)
    public void pkConstraints() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/pkConstraint");
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for synonyms.
     */
    @Test
    public void synonym() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/synonym");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void itShouldCleanCheckConstraint() throws Exception {
        // given
        flyway.setLocations("migration/dbsupport/sqlserver/sql/checkConstraint");
        flyway.migrate();

        // when
        flyway.clean();

        // then
        int pendingMigrations = flyway.info().pending().length;
        assertEquals(3, pendingMigrations);
    }

    /**
     * Tests a large migration that has been reported to hang on SqlServer 2005.
     */
    @Ignore("Axel: Fails due to nested transaction being opened in script, causing outer transaction not to receive COMMIT statement")
    @Test
    public void large() throws Exception {
        flyway.setLocations("migration/dbsupport/sqlserver/sql/large",
                "org.flywaydb.core.internal.dbsupport.sqlserver.large");
        flyway.setTarget(MigrationVersion.fromVersion("3.1.0"));
        flyway.migrate();

        assertEquals("3.1.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        assertTrue(jdbcTemplate.queryForInt("SELECT COUNT(*) FROM dbo.CHANGELOG") > 0);
    }
	
  /**
   * Tests that dml errors that occur in the middle of a batch are correctly detected
   * see issue 718
   */
  @Test
  public void dmlErrorsCorrectlyDetected() throws Exception {
    String tableName = "sample_table";

    flyway.setLocations("migration/dbsupport/sqlserver/sql/dmlErrorDetection");
    Map<String, String> placeholders = new HashMap<String, String>();
    placeholders.put("tableName", dbSupport.quote(tableName));
    flyway.setPlaceholders(placeholders);

    try {
      flyway.migrate();
      fail("This migration should have failed and this point shouldn't have been reached");
    } catch (FlywaySqlScriptException e) {
      // root cause of exception must be defined, and it should be FlywaySqlScriptException
      assertNotNull(e.getCause());
      assertTrue(e.getCause() instanceof SQLException);
      // and make sure the failed statement was properly recorded
      assertEquals(23, e.getLineNumber());
      assertTrue(e.getStatement().contains("INSERT INTO"));
      assertTrue(e.getStatement().contains("VALUES(1)"));
    }
  }

    @Override
    @Ignore("Not supported on SQL Server")
    public void setCurrentSchema() throws Exception {
        //Skip
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE TABLE [schema_version] (\n" +
                "    [version_rank] INT NOT NULL,\n" +
                "    [installed_rank] INT NOT NULL,\n" +
                "    [version] NVARCHAR(50) NOT NULL,\n" +
                "    [description] NVARCHAR(200),\n" +
                "    [type] NVARCHAR(20) NOT NULL,\n" +
                "    [script] NVARCHAR(1000) NOT NULL,\n" +
                "    [checksum] INT,\n" +
                "    [installed_by] NVARCHAR(100) NOT NULL,\n" +
                "    [installed_on] DATETIME NOT NULL DEFAULT GETDATE(),\n" +
                "    [execution_time] INT NOT NULL,\n" +
                "    [success] BIT NOT NULL\n" +
                ")");
        jdbcTemplate.execute("ALTER TABLE [schema_version] ADD CONSTRAINT [schema_version_pk] PRIMARY KEY ([version])");
        jdbcTemplate.execute("CREATE INDEX [schema_version_vr_idx] ON [schema_version] ([version_rank])");
        jdbcTemplate.execute("CREATE INDEX [schema_version_ir_idx] ON [schema_version] ([installed_rank])");
        jdbcTemplate.execute("CREATE INDEX [schema_version_s_idx] ON [schema_version] ([success])");
    }
}
