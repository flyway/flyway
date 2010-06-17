/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core.h2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.code.flyway.core.Flyway;
import com.google.code.flyway.core.SchemaVersion;

/**
 * Test to demonstrate the migration functionality using Hsql.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:migration/h2/h2-context.xml"})
public class H2MediumTest {
    @Autowired
    private Flyway flyway;

    @Test
    public void createAndMigrate() throws Exception {
        assertTrue(flyway.getMetaDataTable().exists());
        SchemaVersion schemaVersion = flyway.getMetaDataTable().latestAppliedMigration().getVersion();
        assertEquals("1.1", schemaVersion.getVersion());
        assertEquals("Populate table", schemaVersion.getDescription());
        assertEquals(0, flyway.migrate());
    }
}