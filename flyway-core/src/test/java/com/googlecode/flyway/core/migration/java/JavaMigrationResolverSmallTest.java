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

import com.googlecode.flyway.core.migration.Migration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for JavaMigrationResolver.
 */
public class JavaMigrationResolverSmallTest {
    @Test
    public void resolveMigrations() {
        JavaMigrationResolver javaMigrationResolver =
                new JavaMigrationResolver("com/googlecode/flyway/core/migration/java/dummy");
        Collection<Migration> migrations = javaMigrationResolver.resolveMigrations();

        assertEquals(2, migrations.size());

        List<Migration> migrationList = new ArrayList<Migration>(migrations);
        Collections.sort(migrationList);

        assertEquals("2", migrationList.get(0).getVersion().toString());
        assertEquals("3.5", migrationList.get(1).getVersion().toString());

        assertEquals("InterfaceBasedMigration", migrationList.get(0).getDescription());
        assertEquals("Three Dot Five", migrationList.get(1).getDescription());

        assertNull(migrationList.get(0).getChecksum());
        assertEquals(35, migrationList.get(1).getChecksum().intValue());
    }
}
