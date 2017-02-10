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
package org.flywaydb.core.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class ConcurrentMigrationTestCase {
    private static final Log LOG = LogFactory.getLog(ConcurrentMigrationTestCase.class);

    /**
     * The number of threads to use in this test.
     */
    private static final int NUM_THREADS = 10;

    /**
     * The quoted schema placeholder for the tests.
     */
    private String schemaQuoted;

    /**
     * Error message in case the concurrent test has failed.
     */
    private String error;

    /**
     * The datasource to use for concurrent migration tests.
     */
    private DataSource concurrentMigrationDataSource;

    /**
     * The instance under test.
     */
    private Flyway flyway;
    private String schemaName;

    @Before
    public void setUp() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        concurrentMigrationDataSource = createDataSource(customProperties);

        Connection connection = concurrentMigrationDataSource.getConnection();
        final DbSupport dbSupport = DbSupportFactory.createDbSupport(connection, false);
        schemaName = getSchemaName(dbSupport);
        schemaQuoted = dbSupport.quote(schemaName);
        connection.close();

        flyway = createFlyway();
        flyway.clean();

        if (needsBaseline()) {
            flyway.baseline();
        }
    }

    protected boolean needsBaseline() {
        return false;
    }

    protected String getBasedir() {
        return "migration/concurrent";
    }

    protected String getSchemaName(DbSupport dbSupport) {
        return "concurrent_test";
    }

    /**
     * Creates the datasource for this testcase based on these optional custom properties from the user home.
     *
     * @param customProperties The optional custom properties.
     * @return The new datasource.
     */
    protected abstract DataSource createDataSource(Properties customProperties) throws Exception;

    @Test
    public void migrateConcurrently() throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    createFlyway().migrate();
                } catch (Exception e) {
                    LOG.error("Migrate failed", e);
                    error = e.getMessage();
                }
            }
        };

        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(runnable);
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
        }

        assertNull(error, error);
        final MigrationInfo[] applied = flyway.info().applied();
        int expected = 4;
        if (applied[0].getType() == MigrationType.SCHEMA) {
            expected++;
        }
        if (needsBaseline()) {
            expected++;
        }
        assertEquals(expected, applied.length);
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(0, flyway.migrate());

        Connection connection = null;
        try {
            connection = concurrentMigrationDataSource.getConnection();
            assertEquals(2, new JdbcTemplate(connection, 0).queryForInt(
                    "SELECT COUNT(*) FROM " + schemaQuoted + ".test_user"));
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }

    private Flyway createFlyway() throws SQLException {
        Flyway newFlyway = new Flyway();
        newFlyway.setDataSource(concurrentMigrationDataSource);
        newFlyway.setLocations(getBasedir());
        newFlyway.setSchemas(schemaName);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", schemaQuoted);

        newFlyway.setPlaceholders(placeholders);
        newFlyway.setBaselineVersionAsString("0.1");
        return newFlyway;
    }
}
