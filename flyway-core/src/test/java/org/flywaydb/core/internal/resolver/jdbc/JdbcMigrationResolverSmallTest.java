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
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.FlywayConfigurationForTests;
import org.flywaydb.core.internal.resolver.jdbc.dummy.V2__InterfaceBasedMigration;
import org.flywaydb.core.internal.resolver.jdbc.dummy.Version3dot5;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for JdbcMigrationResolver.
 */
public class JdbcMigrationResolverSmallTest {
    private final Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader());
    private final FlywayConfiguration config = FlywayConfigurationForTests.create();

    @Test(expected = FlywayException.class)
    public void broken() {
        new JdbcMigrationResolver(scanner, new Location("org/flywaydb/core/internal/resolver/jdbc/error"), config).resolveMigrations();
    }

    @Test
    public void resolveMigrations() throws SQLException {
        JdbcMigrationResolver jdbcMigrationResolver =
                new JdbcMigrationResolver(scanner, new Location("org/flywaydb/core/internal/resolver/jdbc/dummy"), config);
        Collection<ResolvedMigration> migrations = jdbcMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        ResolvedMigration migrationInfo = migrationList.get(0);
        assertEquals("2", migrationInfo.getVersion().toString());
        assertEquals("InterfaceBasedMigration", migrationInfo.getDescription());
        assertNull(migrationInfo.getChecksum());

        // do a test execute, since the migration does not do anything, we simply test whether the
        // configuration has been set correctly
        migrationInfo.getExecutor().execute(null);

        ResolvedMigration migrationInfo1 = migrationList.get(1);
        assertEquals("3.5", migrationInfo1.getVersion().toString());
        assertEquals("Three Dot Five", migrationInfo1.getDescription());
        assertEquals(35, migrationInfo1.getChecksum().intValue());

        ResolvedMigration migrationInfo2 = migrationList.get(2);
        assertEquals("4", migrationInfo2.getVersion().toString());
    }

    @Test
    public void conventionOverConfiguration() {
        JdbcMigrationResolver jdbcMigrationResolver = new JdbcMigrationResolver(scanner, null, null);
        ResolvedMigration migrationInfo = jdbcMigrationResolver.extractMigrationInfo(new V2__InterfaceBasedMigration());
        assertEquals("2", migrationInfo.getVersion().toString());
        assertEquals("InterfaceBasedMigration", migrationInfo.getDescription());
        assertNull(migrationInfo.getChecksum());
    }

    @Test
    public void explicitInfo() {
        JdbcMigrationResolver jdbcMigrationResolver = new JdbcMigrationResolver(scanner, null, null);
        ResolvedMigration migrationInfo = jdbcMigrationResolver.extractMigrationInfo(new Version3dot5());
        assertEquals("3.5", migrationInfo.getVersion().toString());
        assertEquals("Three Dot Five", migrationInfo.getDescription());
        assertEquals(35, migrationInfo.getChecksum().intValue());
    }
}
