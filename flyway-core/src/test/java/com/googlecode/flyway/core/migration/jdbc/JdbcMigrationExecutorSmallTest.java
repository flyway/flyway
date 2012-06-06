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
package com.googlecode.flyway.core.migration.jdbc;

import com.googlecode.flyway.core.migration.jdbc.dummy.V2__InterfaceBasedMigration;
import com.googlecode.flyway.core.migration.jdbc.dummy.Version3dot5;
import com.googlecode.flyway.core.migration.spring.SpringJdbcMigrationExecutor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test the info extraction of the JdbcMigrationExecutor.
 */
public class JdbcMigrationExecutorSmallTest {
    @Test
    public void conventionOverConfiguration() {
        JdbcMigrationExecutor jdbcMigrationExecutor = new JdbcMigrationExecutor(new V2__InterfaceBasedMigration());
        assertEquals("2", jdbcMigrationExecutor.getVersion().toString());
        assertEquals("InterfaceBasedMigration", jdbcMigrationExecutor.getDescription());
        assertNull(jdbcMigrationExecutor.getChecksum());
    }

    @Test
    public void explicitInfo() {
        JdbcMigrationExecutor jdbcMigrationExecutor = new JdbcMigrationExecutor(new Version3dot5());
        assertEquals("3.5", jdbcMigrationExecutor.getVersion().toString());
        assertEquals("Three Dot Five", jdbcMigrationExecutor.getDescription());
        assertEquals(35, jdbcMigrationExecutor.getChecksum().intValue());
    }
}
