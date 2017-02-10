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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using DB2.
 */
@Category(DbCategory.DB2.class)
public class DB2MigrationMediumTest extends MigrationTestCase {
    private String user;

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        user = customProperties.getProperty("db2.user", "db2admin");
        String password = customProperties.getProperty("db2.password", "flyway");
        String url = customProperties.getProperty("db2.url", "jdbc:db2://localhost:50000/flyway");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    @Ignore("Excluding by default as for some reason this test is flaky in Maven even though it is stable in IntelliJ")
    public void schemaWithDash() throws FlywayException {
        flyway.setSchemas("my-schema");
        flyway.setLocations(getBasedir());
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/sequence");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Sequence", flyway.info().current().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("VALUES NEXTVAL FOR BEAST_SEQ"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void bitdata() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/bitdata");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void delimiter() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/delimiter");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void mqt() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/mqt");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Mqt", flyway.info().current().getDescription());

        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM empl_mqt"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void alias() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/alias");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Alias", flyway.info().current().getDescription());

        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM POOR_SLAVE"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/trigger");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void procedure() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/procedure");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void type() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/type");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void function() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/function");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void expressionBasedIndex() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/index");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void versioned() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/versioned");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    // Issue #802: Clean on DB2 does not clean triggers.
    @Test
    public void noTriggersShouldBeLeftAfterClean() throws Exception {
        flyway.setLocations("migration/dbsupport/db2/sql/trigger");
        flyway.migrate();
        flyway.clean();

        // default schema is username in upper case, so we need to use that.
        assertEquals(0, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSCAT.TRIGGERS WHERE TRIGSCHEMA = ?", user.toUpperCase()));
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE TABLE \"schema_version\" (\n" +
                "    \"version_rank\" INT NOT NULL,\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50) NOT NULL,\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL,\n" +
                "    CONSTRAINT \"schema_version_s\" CHECK (\"success\" in(0,1))\n" +
                ")");
        jdbcTemplate.execute("ALTER TABLE \"schema_version\" ADD CONSTRAINT \"schema_version_pk\" PRIMARY KEY (\"version\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_vr_idx\" ON \"schema_version\" (\"version_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_ir_idx\" ON \"schema_version\" (\"installed_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_s_idx\" ON \"schema_version\" (\"success\")");
    }
}
