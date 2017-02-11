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
package org.flywaydb.core.internal.dbsupport.informix;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test to demonstrate the migration functionality using Informix.
 */
@Category(DbCategory.Informix.class)
public class InformixMigrationMediumTest extends MigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {

        String user = customProperties.getProperty("informix.user", "informix");
        String password = customProperties.getProperty("informix.password", "in4mix");
        String url = customProperties.getProperty("informix.url", "jdbc:informix-sqli://localhost:9088/test:informixserver=dev");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    /**
     * Tests parsing of object names that contain keywords such as MY_TABLE.
     */
    @Test
    public void objectNames() throws FlywayException {
        flyway.setLocations("migration/dbsupport/informix/sql/objectnames");
        flyway.migrate();
    }

    /**
     * Test clean with recycle bin
     *
     * @throws java.lang.Exception
     */
//    @Ignore
//    @Test
//    public void cleanWithRecycleBin() throws Exception {
//    }
    /**
     * Tests support for create procedure.
     */
    @Test
    public void procedure() throws FlywayException {
        flyway.setLocations("migration/dbsupport/informix/sql/procedure");
        flyway.migrate();
    }

    /**
     * Tests support for create trigger. Ensures that a Statement is used
     * instead of a PreparedStatement. Also ensures that schema-level triggers
     * are properly cleaned.
     */
    @Test
    public void trigger() throws FlywayException {
        flyway.setLocations("migration/dbsupport/informix/sql/trigger");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for clean together with cluster Tables.
     */
    @Test
    @Ignore
    public void cluster() throws FlywayException {
    }

    /**
     * Tests support for clean together with XML Type.
     */
    @Ignore
    @Test
    public void xml() throws FlywayException {

    }

    /**
     * Tests support for cleaning of tables with Flashback/Total Recall enabled.
     * Schema containing such tables has to be first cleaned by disabling
     * flashback on each table;
     */
    @Ignore
    @Test
    public void flashback() throws FlywayException {
    }

    /**
     * Tests support for reference partitioned tables.
     */
    @Ignore
    @Test
    public void referencePartitionedTable() throws FlywayException {

    }

    /**
     * Tests support for cleaning together with JAVA SOURCE Type.
     *
     * @throws java.sql.SQLException
     */
    @Ignore
    @Test
    public void javaSource() throws FlywayException, SQLException {
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("create table schema_version ( \n"
                + "version_rank INT NOT NULL, \n"
                + "installed_rank INT NOT NULL, \n"
                + "version VARCHAR(50) NOT NULL, \n"
                + "description VARCHAR(200) NOT NULL, \n"
                + "type1 VARCHAR(20) NOT NULL, \n"
                + "script TEXT NOT NULL, \n"
                + "checksum INT, \n"
                + "installed_by VARCHAR(100) NOT NULL, \n"
                + "installed_on DATETIME YEAR TO FRACTION NOT NULL DEFAULT CURRENT\n"
                + "execution_time INT NOT NULL, \n"
                + "success SMALLINT NOT NULL \n"
                + " )");

        jdbcTemplate.execute("ALTER TABLE \"schema_version\" ADD CONSTRAINT \"schema_version_pk\" PRIMARY KEY (\"version\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_vr_idx\" ON \"schema_version\" (\"version_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_ir_idx\" ON \"schema_version\" (\"installed_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_s_idx\" ON \"schema_version\" (\"success\")");
    }
}
