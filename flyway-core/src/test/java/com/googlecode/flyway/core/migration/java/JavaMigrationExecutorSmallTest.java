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
package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.java.dummy.V2__InterfaceBasedMigration;
import com.googlecode.flyway.core.migration.java.dummy.Version3dot5;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test the info extraction of the JavaMigrationExecutor.
 */
public class JavaMigrationExecutorSmallTest {
    @Test
    public void conventionOverConfiguration() {
        JavaMigrationExecutor javaMigrationExecutor = new JavaMigrationExecutor(new V2__InterfaceBasedMigration());
        assertEquals("2", javaMigrationExecutor.getVersion().toString());
        assertEquals("InterfaceBasedMigration", javaMigrationExecutor.getDescription());
        assertNull(javaMigrationExecutor.getChecksum());
    }

    @Test
    public void explicitInfo() {
        JavaMigrationExecutor javaMigrationExecutor = new JavaMigrationExecutor(new Version3dot5());
        assertEquals("3.5", javaMigrationExecutor.getVersion().toString());
        assertEquals("Three Dot Five", javaMigrationExecutor.getDescription());
        assertEquals(35, javaMigrationExecutor.getChecksum().intValue());
    }
}
