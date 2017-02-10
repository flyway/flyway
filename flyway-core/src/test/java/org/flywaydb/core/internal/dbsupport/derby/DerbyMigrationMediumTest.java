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
package org.flywaydb.core.internal.dbsupport.derby;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using Derby.
 */
@Category(DbCategory.Derby.class)
public class DerbyMigrationMediumTest extends MigrationTestCase {
    static {
        System.setProperty("derby.stream.error.field", "java.lang.System.err");
    }

    @Override
    public void tearDown() throws Exception {
        try {
            new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:derby:memory:flyway_db;drop=true", "", "", null).getConnection();
        } catch (FlywayException e) {
            //OK, expected error 08006. See http://db.apache.org/derby/docs/dev/devguide/cdevdvlpinmemdb.html
        }

        super.tearDown();
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:derby:memory:flyway_db;create=true", "", "", null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void bitdata() throws Exception {
        flyway.setLocations("migration/dbsupport/derby/sql/bitdata");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/dbsupport/derby/sql/trigger");
        flyway.migrate();

        // Fails if triggers aren't cleaned properly
        flyway.clean();
    }

    @Test
    public void validateOnMigrate() {
        flyway.setLocations("migration/sql");
        flyway.setSchemas("non-existant");
        flyway.setValidateOnMigrate(true);
        flyway.migrate();
    }

    @Test
    public void testFlyway1331() throws Exception {
        try {
            Flyway flyway = new Flyway();
            flyway.setDataSource("jdbc:derby:memory:fw1331db;create=true", "sa", "sa");
            flyway.setLocations("migration/sql");
            flyway.setBaselineOnMigrate(true);
            flyway.migrate();
        } finally {
            try {
                JdbcUtils.closeConnection(DriverManager.getConnection("jdbc:derby:memory:fw1331db;shutdown=true", "sa", "sa"));
            } catch (Exception e) {
                // Suppress
            }
        }
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
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ")");
        jdbcTemplate.execute("ALTER TABLE \"schema_version\" ADD CONSTRAINT \"schema_version_pk\" PRIMARY KEY (\"version\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_vr_idx\" ON \"schema_version\" (\"version_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_ir_idx\" ON \"schema_version\" (\"installed_rank\")");
        jdbcTemplate.execute("CREATE INDEX \"schema_version_s_idx\" ON \"schema_version\" (\"success\")");
    }
}