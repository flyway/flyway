/**
 * Copyright 2010-2015 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.FlywayConfiguration;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.FlywayConfigurationForTests;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for SqlMigration.
 */
public class SqlMigrationResolverSmallTest {

    @Test
    public void resolveMigrations() {
        FlywayConfiguration config = new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "V", "__", ".sql");

        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, config,
                        new Location("migration/subdir"), PlaceholderReplacer.NO_PLACEHOLDERS);
        Collection<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("1.1", migrationList.get(1).getVersion().toString());
        assertEquals("2.0", migrationList.get(2).getVersion().toString());

        assertEquals("dir1/V1__First.sql", migrationList.get(0).getScript());
        assertEquals("V1_1__Populate_table.sql", migrationList.get(1).getScript());
        assertEquals("dir2/V2_0__Add_foreign_key.sql", migrationList.get(2).getScript());
    }

    @Test
    public void resolveMigrationsRoot() {
        FlywayConfiguration config = new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "CheckValidate", "__", ".sql");
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, config, new Location(""),
                        PlaceholderReplacer.NO_PLACEHOLDERS);

        //changed to 2 as new test cases are added for SybaseASE
        assertEquals(2, sqlMigrationResolver.resolveMigrations().size());
    }

    @Test
    public void resolveMigrationsNonExisting() {
        FlywayConfiguration config = new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "CheckValidate", "__", ".sql");
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, config,
                        new Location("non/existing"), PlaceholderReplacer.NO_PLACEHOLDERS);

        sqlMigrationResolver.resolveMigrations();
    }

    @Test
    public void extractScriptName() {
        FlywayConfiguration config = new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "db_", "__", ".sql");
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, config,
                        new Location("db/migration"), PlaceholderReplacer.NO_PLACEHOLDERS);

        assertEquals("db_0__init.sql", sqlMigrationResolver.extractScriptName(
                new ClassPathResource("db/migration/db_0__init.sql", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameRootLocation() {
        FlywayConfiguration config = new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "db_", "__", ".sql");
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, config, new Location(""),
                        PlaceholderReplacer.NO_PLACEHOLDERS);

        assertEquals("db_0__init.sql", sqlMigrationResolver.extractScriptName(
                new ClassPathResource("db_0__init.sql", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameFileSystemPrefix() {
        FlywayConfiguration config = new FlywayConfigurationForTests(Thread.currentThread().getContextClassLoader(), new String[0], "UTF-8", "V", "__", ".sql");
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, config,
                        new Location("filesystem:/some/dir"), PlaceholderReplacer.NO_PLACEHOLDERS);

        assertEquals("V3.171__patch.sql", sqlMigrationResolver.extractScriptName(new FileSystemResource("/some/dir/V3.171__patch.sql")));
    }
}
