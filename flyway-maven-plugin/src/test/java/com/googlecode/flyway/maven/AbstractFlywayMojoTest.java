/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertFalse;

/**
 * Test for the AbstractFlywayMojo.
 */
public class AbstractFlywayMojoTest {
    /**
     * Tests that the datasource is properly closed and not leaking connections.
     */
    @Test
    public void dataSourceClosed() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
                flyway.init();
            }
        };
        assertNoConnectionLeak(mojo);
    }

    /**
     * Tests that the datasource is properly closed and not leaking connections, even when an exception was thrown.
     */
    @Test
    public void dataSourceClosedAfterException() throws Exception {
        AbstractFlywayMojo mojo = new AbstractFlywayMojo() {
            @Override
            protected void doExecute(Flyway flyway) throws Exception {
                flyway.init();
                throw new FlywayException("Wanted failure");
            }
        };
        assertNoConnectionLeak(mojo);
    }

    /**
     * Asserts that this mojo does not leak DB connections when executed.
     *
     * @param mojo The mojo to check.
     */
    private void assertNoConnectionLeak(AbstractFlywayMojo mojo) throws Exception {
        mojo.driver = "org.h2.Driver";
        mojo.url = "jdbc:h2:mem:flyway_leak_test";
        mojo.user = "SA";

        try {
            mojo.execute();
        } catch (Exception e) {
            // Ignoring. The exception is not what we're testing.
        }

        BasicDataSource dataSource = mojo.createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        H2DbSupport h2DbSupport = new H2DbSupport(jdbcTemplate);
        boolean tableStillPresent = h2DbSupport.tableExists("schema_version");
        dataSource.close();
        assertFalse(tableStillPresent);
    }
}
