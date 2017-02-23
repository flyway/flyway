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
package org.flywaydb.core.internal.dbsupport.sybase.ase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Test to demonstrate the migration functionality using Sybase AES with the Jtds driver.
 *
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.SybaseASE.class)
public class SybaseASEMigrationMediumTest extends MigrationTestCase {
	
	protected static final String BASEDIR = "migration/dbsupport/sybaseASE/sql";
	
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("sybase.user", "sa");
        String password = customProperties.getProperty("sybase.password", "test");
        String url = customProperties.getProperty("sybase.jtds_url", "jdbc:jtds:sybase://127.0.0.1:5100/flyway_test");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

	@Override
	protected String getQuoteLocation() {
		//Sybase does not support quoting table names
		return getValidateLocation();
	}
	
	@Ignore("Table quote is not supported in sybase.")
	@Override
	public void quotesAroundTableName() {
	}

	@Override
    public void outOfOrderMultipleRankIncrease() {
        flyway.setLocations(BASEDIR);
        flyway.migrate();

        flyway.setLocations(BASEDIR, "migration/dbsupport/sybaseASE/outoforder");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        assertEquals(org.flywaydb.core.api.MigrationState.OUT_OF_ORDER, flyway.info().all()[2].getState());
    }
	
	@Override
    public void subDir() {
        flyway.setLocations("migration/dbsupport/sybaseASE/subdir");
        assertEquals(3, flyway.migrate());
    }
	
	@Override
    @Ignore("Not supported on Sybase ASE Server")
    public void setCurrentSchema() throws Exception {
        //Skip
    }
	
	@Override
	@Ignore("Schema reference is not supported on Sybase ASE server")
	public void migrateMultipleSchemas() throws Exception {
		//Skip
	}
	
	@Override
	@Ignore("Table name quote is not supported on Sybase ASE server")
    public void quote() throws Exception {
        //skip
    }
	
	@Override
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations("migration/dbsupport/sybaseASE/failed");
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
            // It is line 22 as a go statement is added for Sybase
            assertEquals(22, e.getLineNumber());
            String statement = e.getStatement();
            if (statement.indexOf('\n') != -1) {
            	statement = statement.replace("\n", "");
            }
            assertEquals("THIS IS NOT VALID SQL", statement);
        }

        MigrationInfo migration = flyway.info().current();
        assertEquals(
                dbSupport.supportsDdlTransactions(),
                !dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable(tableName).exists());
        if (dbSupport.supportsDdlTransactions()) {
            assertNull(migration);
        } else {
            MigrationVersion version = migration.getVersion();
            assertEquals("1", version.toString());
            assertEquals("Should Fail", migration.getDescription());
            assertEquals(MigrationState.FAILED, migration.getState());
            assertEquals(1, flyway.info().applied().length);
        }
    }

	//Location override the parent test cases begins
	
	@Override
	protected String getBasedir() {
		return BASEDIR;
	}

	@Override
	protected String getFutureFailedLocation() {
		return "migration/dbsupport/sybaseASE/future_failed";
	}
	
	@Override
	protected String getValidateLocation() {
		return "migration/dbsupport/sybaseASE/validate";
	}
	
	@Override
	protected String getSemiColonLocation() {
		return "migration/dbsupport/sybaseASE/semicolon";
	}
	
	@Override
	protected String getCommentLocation() {
		return "migration/dbsupport/sybaseASE/comment";
	}

    @Ignore("Not needed as Sybase ASE support was first introduced in Flyway 4.0")
    @Override
    public void upgradeMetadataTableTo40Format() throws Exception {
    }
}
