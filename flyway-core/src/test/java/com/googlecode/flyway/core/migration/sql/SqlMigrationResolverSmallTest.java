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
package com.googlecode.flyway.core.migration.sql;

import com.googlecode.flyway.core.migration.Migration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for SqlMigration.
 */
public class SqlMigrationResolverSmallTest {
    /**
     * Test for extractVersionStringFromFileName.
     */
    @Test
    public void extractVersionStringFromFileName() {
        assertEquals("8_0", SqlMigrationResolver.extractVersionStringFromFileName("sql/V8_0.sql", "V", ".sql"));
        assertEquals("9_0__CommentAboutContents", SqlMigrationResolver.extractVersionStringFromFileName("sql/V9_0__CommentAboutContents.sql", "V", ".sql"));
    }

    @Test
    public void resolveMigrations() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("/migration/subdir", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", ".sql");
        Collection<Migration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<Migration> migrationList = new ArrayList<Migration>(migrations);
        Collections.sort(migrationList);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("1.1", migrationList.get(1).getVersion().toString());
        assertEquals("2.0", migrationList.get(2).getVersion().toString());

        assertEquals("dir1/V1__First.sql", migrationList.get(0).getScript());
        assertEquals("V1_1__Populate_table.sql", migrationList.get(1).getScript());
        assertEquals("dir2/V2_0__Add_foreign_key.sql", migrationList.get(2).getScript());
    }

    @Test
    public void resolveMigrationsNoLeadingSlash() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("migration/subdir", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", ".sql");
        Collection<Migration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<Migration> migrationList = new ArrayList<Migration>(migrations);
        Collections.sort(migrationList);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("1.1", migrationList.get(1).getVersion().toString());
        assertEquals("2.0", migrationList.get(2).getVersion().toString());

        assertEquals("dir1/V1__First.sql", migrationList.get(0).getScript());
        assertEquals("V1_1__Populate_table.sql", migrationList.get(1).getScript());
        assertEquals("dir2/V2_0__Add_foreign_key.sql", migrationList.get(2).getScript());
    }

    @Test
    public void resolveMigrationsRoot() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "CheckValidate", ".sql");
        Collection<Migration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(1, migrations.size());
    }

    @Test
    public void resolveMigrationsNonExisting() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("non/existing", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "CheckValidate", ".sql");
        Collection<Migration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(0, migrations.size());
    }
}
