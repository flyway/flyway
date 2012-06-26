/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.Flyway;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test to demonstrate the migration functionality using H2.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class ConcurrentMigrationTestCase {
    /**
     * The number of threads to use in this test.
     */
    private static final int NUM_THREADS = 10;

    /**
     * The directory containing the migrations for the tests.
     */
    private static final String BASE_DIR = "migration/sql";

    /**
     * Flag to indicate the concurrent test has failed.
     */
    private boolean failed;

    /**
     * The datasource to use for concurrent migration tests.
     */
    private DataSource concurrentMigrationDataSource;

    /**
     * The instance under test.
     */
    private Flyway flyway;

    @Before
    public void setUp() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        concurrentMigrationDataSource = createDataSource(customProperties);

        flyway = new Flyway();
        flyway.setDataSource(concurrentMigrationDataSource);
        flyway.setLocations(BASE_DIR);
        flyway.clean();
        flyway.init();
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
                    Flyway flyway2 = new Flyway();
                    flyway2.setDataSource(concurrentMigrationDataSource);
                    flyway2.setLocations(BASE_DIR);
                    flyway2.migrate();
                } catch (Exception e) {
                    e.printStackTrace();
                    failed = true;
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

        assertFalse(failed);
        assertEquals(5, flyway.history().size());
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());
        assertEquals(0, flyway.migrate());
    }
}
