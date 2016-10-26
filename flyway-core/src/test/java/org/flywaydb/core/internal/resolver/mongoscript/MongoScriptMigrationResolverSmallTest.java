/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.mongoscript;

import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MongoFlywayConfigurationForTests;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for MongoScript migration resolver.
 */
public class MongoScriptMigrationResolverSmallTest {

    private final Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader());

    @Test
    public void resolveMigrations() {
        MongoScriptMigrationResolver mongoMigrationResolver =
                new MongoScriptMigrationResolver(scanner,
                        new Location("migration/subdir"),
                        PlaceholderReplacer.NO_PLACEHOLDERS,
                        MongoFlywayConfigurationForTests.create());
        Collection<ResolvedMigration> migrations = mongoMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals("1.1", migrationList.get(0).getVersion().toString());
        assertEquals("1.2", migrationList.get(1).getVersion().toString());
        assertEquals("2.0", migrationList.get(2).getVersion().toString());

        assertEquals("dir1/V1_1__Add_users.js", migrationList.get(0).getScript());
        assertEquals("V1_2__Delete_Mallory.js", migrationList.get(1).getScript());
        assertEquals("dir2/V2_0__Update_Bob_age.js", migrationList.get(2).getScript());
    }

    @Test
    public void resolveMigrationsRoot() {
        MongoFlywayConfigurationForTests configuration =
                MongoFlywayConfigurationForTests.createWithPrefix("CheckValidate");
        configuration.setRepeatableMongoMigrationPrefix("X");
        MongoScriptMigrationResolver mongoMigrationResolver =
            new MongoScriptMigrationResolver(scanner,
                                             new Location(""),
                                             PlaceholderReplacer.NO_PLACEHOLDERS,
                                             configuration);
        assertEquals(2, mongoMigrationResolver.resolveMigrations().size());
    }

    @Test
    public void resolveMigrationsNonExisting() {
        MongoScriptMigrationResolver mongoMigrationResolver =
            new MongoScriptMigrationResolver(scanner,
                                             new Location("non/existing"),
                                             PlaceholderReplacer.NO_PLACEHOLDERS,
                                             MongoFlywayConfigurationForTests.createWithPrefix("CheckValidate"));

        mongoMigrationResolver.resolveMigrations();
    }

    @Test
    public void extractScriptName() {
        MongoScriptMigrationResolver mongoMigrationResolver =
            new MongoScriptMigrationResolver(scanner,
                                             new Location("db/migration"),
                                             PlaceholderReplacer.NO_PLACEHOLDERS,
                                             MongoFlywayConfigurationForTests.createWithPrefix("db_"));

        assertEquals("db_0__init.js", mongoMigrationResolver.extractScriptName(
                new ClassPathResource("db/migration/db_0__init.js", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameRootLocation() {
        MongoScriptMigrationResolver mongoMigrationResolver =
            new MongoScriptMigrationResolver(scanner,
                                             new Location(""),
                                             PlaceholderReplacer.NO_PLACEHOLDERS,
                                             MongoFlywayConfigurationForTests.createWithPrefix("db_"));

        assertEquals("db_0__init.js", mongoMigrationResolver.extractScriptName(
                new ClassPathResource("db_0__init.js", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameFileSystemPrefix() {
        MongoScriptMigrationResolver mongoMigrationResolver =
            new MongoScriptMigrationResolver(scanner,
                                             new Location("filesystem:/some/dir"),
                                             PlaceholderReplacer.NO_PLACEHOLDERS,
                                             MongoFlywayConfigurationForTests.create());

        assertEquals("V3.171__patch.js",
                mongoMigrationResolver.extractScriptName(new FileSystemResource("/some/dir/V3.171__patch.js")));
    }

    @Test
    public void isMongoScriptCallback() {
        assertTrue(MongoScriptMigrationResolver.isMongoScriptCallback("afterMigrate.js", ".js"));
        assertFalse(MongoScriptMigrationResolver.isMongoScriptCallback("V1__afterMigrate.js", ".js"));
    }

    @Test
    public void calculateChecksum() {
        assertEquals(MongoScriptMigrationResolver.calculateChecksum(null, "abc\ndef efg\nxyz"),
                MongoScriptMigrationResolver.calculateChecksum(null, "abc\r\ndef efg\nxyz\r\n"));
    }
}
