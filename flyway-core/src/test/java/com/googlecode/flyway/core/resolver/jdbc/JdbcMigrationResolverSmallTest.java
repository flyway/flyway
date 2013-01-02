/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.resolver.jdbc;

import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.resolver.jdbc.dummy.V2__InterfaceBasedMigration;
import com.googlecode.flyway.core.resolver.jdbc.dummy.Version3dot5;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for JdbcMigrationResolver.
 */
public class JdbcMigrationResolverSmallTest {
    @Test
    public void resolveMigrations() {
        JdbcMigrationResolver jdbcMigrationResolver =
                new JdbcMigrationResolver("com/googlecode/flyway/core/resolver/jdbc/dummy");
        Collection<ResolvedMigration> migrations = jdbcMigrationResolver.resolveMigrations();

        assertEquals(2, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);
        Collections.sort(migrationList);

        ResolvedMigration migrationInfo = migrationList.get(0);
        assertEquals("2", migrationInfo.getVersion().toString());
        assertEquals("InterfaceBasedMigration", migrationInfo.getDescription());
        assertNull(migrationInfo.getChecksum());

        ResolvedMigration migrationInfo1 = migrationList.get(1);
        assertEquals("3.5", migrationInfo1.getVersion().toString());
        assertEquals("Three Dot Five", migrationInfo1.getDescription());
        assertEquals(35, migrationInfo1.getChecksum().intValue());
    }

    @Test
    public void conventionOverConfiguration() {
        JdbcMigrationResolver jdbcMigrationResolver = new JdbcMigrationResolver(null);
        ResolvedMigration migrationInfo = jdbcMigrationResolver.extractMigrationInfo(new V2__InterfaceBasedMigration());
        assertEquals("2", migrationInfo.getVersion().toString());
        assertEquals("InterfaceBasedMigration", migrationInfo.getDescription());
        assertNull(migrationInfo.getChecksum());
    }

    @Test
    public void explicitInfo() {
        JdbcMigrationResolver jdbcMigrationResolver = new JdbcMigrationResolver(null);
        ResolvedMigration migrationInfo = jdbcMigrationResolver.extractMigrationInfo(new Version3dot5());
        assertEquals("3.5", migrationInfo.getVersion().toString());
        assertEquals("Three Dot Five", migrationInfo.getDescription());
        assertEquals(35, migrationInfo.getChecksum().intValue());
    }
}
