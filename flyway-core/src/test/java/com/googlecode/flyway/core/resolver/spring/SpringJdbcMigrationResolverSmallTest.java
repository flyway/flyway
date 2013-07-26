/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.resolver.spring;

import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.resolver.spring.dummy.V2__InterfaceBasedMigration;
import com.googlecode.flyway.core.resolver.spring.dummy.Version3dot5;
import com.googlecode.flyway.core.util.Location;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for SpringJdbcMigrationResolver.
 */
public class SpringJdbcMigrationResolverSmallTest {
    @Test
    public void resolveMigrations() {
        SpringJdbcMigrationResolver springJdbcMigrationResolver =
                new SpringJdbcMigrationResolver(new Location("com/googlecode/flyway/core/resolver/spring/dummy"));
        Collection<ResolvedMigration> migrations = springJdbcMigrationResolver.resolveMigrations();

        assertEquals(2, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);
        Collections.sort(migrationList);

        assertEquals("2", migrationList.get(0).getVersion().toString());
        assertEquals("3.5", migrationList.get(1).getVersion().toString());

        assertEquals("InterfaceBasedMigration", migrationList.get(0).getDescription());
        assertEquals("Three Dot Five", migrationList.get(1).getDescription());

        assertNull(migrationList.get(0).getChecksum());
        assertEquals(35, migrationList.get(1).getChecksum().intValue());
    }

    @Test
    public void conventionOverConfiguration() {
        SpringJdbcMigrationResolver springJdbcMigrationResolver = new SpringJdbcMigrationResolver(null);
        ResolvedMigration migrationInfo = springJdbcMigrationResolver.extractMigrationInfo(new V2__InterfaceBasedMigration());
        assertEquals("2", migrationInfo.getVersion().toString());
        assertEquals("InterfaceBasedMigration", migrationInfo.getDescription());
        assertNull(migrationInfo.getChecksum());
    }

    @Test
    public void explicitInfo() {
        SpringJdbcMigrationResolver springJdbcMigrationResolver = new SpringJdbcMigrationResolver(null);
        ResolvedMigration migrationInfo = springJdbcMigrationResolver.extractMigrationInfo(new Version3dot5());
        assertEquals("3.5", migrationInfo.getVersion().toString());
        assertEquals("Three Dot Five", migrationInfo.getDescription());
        assertEquals(35, migrationInfo.getChecksum().intValue());
    }
}
