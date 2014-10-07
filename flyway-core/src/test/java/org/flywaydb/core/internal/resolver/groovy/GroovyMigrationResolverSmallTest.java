/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.resolver.groovy;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for GroovyMigration.
 */
public class GroovyMigrationResolverSmallTest {
    @Test
    public void resolveMigrations() {
        GroovyMigrationResolver GroovyMigrationResolver =
                new GroovyMigrationResolver(Thread.currentThread().getContextClassLoader(),
                        new Location("migration/subdirgroovy"), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", "__", ".groovy");
        Collection<ResolvedMigration> migrations = GroovyMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("1.1", migrationList.get(1).getVersion().toString());
        assertEquals("2.0", migrationList.get(2).getVersion().toString());

        assertEquals("dir1/V1__First.groovy", migrationList.get(0).getScript());
        assertEquals("V1_1__Populate_table.groovy", migrationList.get(1).getScript());
        assertEquals("dir2/V2_0__Add_foreign_key.groovy", migrationList.get(2).getScript());
    }

    @Test
    public void resolveMigrationsRoot() {
        GroovyMigrationResolver groovyMigrationResolver =
                new GroovyMigrationResolver(Thread.currentThread().getContextClassLoader(), new Location(""),
                        PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "CheckValidate", "__", ".groovy");

        assertEquals(1  , groovyMigrationResolver.resolveMigrations().size());
    }

    @Test(expected = FlywayException.class)
    public void resolveMigrationsNonExisting() {
        GroovyMigrationResolver groovyMigrationResolver =
                new GroovyMigrationResolver(Thread.currentThread().getContextClassLoader(),
                        new Location("non/existing"), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8",
                        "CheckValidate", "__", ".groovy");

        groovyMigrationResolver.resolveMigrations();
    }

    @Test
    public void extractScriptName() {
        GroovyMigrationResolver groovyMigrationResolver =
                new GroovyMigrationResolver(Thread.currentThread().getContextClassLoader(),
                        new Location("db/migration"), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "db_", "__", ".groovy");

        assertEquals("db_0__init.groovy", groovyMigrationResolver.extractScriptName(
                new ClassPathResource("db/migration/db_0__init.groovy", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameRootLocation() {
        GroovyMigrationResolver groovyMigrationResolver =
                new GroovyMigrationResolver(Thread.currentThread().getContextClassLoader(), new Location(""),
                        PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "db_", "__", ".groovy");

        assertEquals("db_0__init.groovy", groovyMigrationResolver.extractScriptName(
                new ClassPathResource("db_0__init.groovy", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameFileSystemPrefix() {
        GroovyMigrationResolver groovyMigrationResolver =
                new GroovyMigrationResolver(Thread.currentThread().getContextClassLoader(),
                        new Location("filesystem:/some/dir"), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8",
                        "V", "__", ".groovy");

        assertEquals("V3.171__patch.groovy", groovyMigrationResolver.extractScriptName(new FileSystemResource("/some/dir/V3.171__patch.groovy")));
    }
}
