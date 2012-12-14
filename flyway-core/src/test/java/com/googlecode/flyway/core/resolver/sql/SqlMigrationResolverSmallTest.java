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
package com.googlecode.flyway.core.resolver.sql;

import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
                new SqlMigrationResolver("migration/subdir", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", ".sql");
        Collection<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);
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

        assertEquals(1, sqlMigrationResolver.resolveMigrations().size());
    }

    @Test
    public void resolveMigrationsNonExisting() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("non/existing", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "CheckValidate", ".sql");

        assertTrue(sqlMigrationResolver.resolveMigrations().isEmpty());
    }

    @Test
    public void extractScriptName() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("db/migration", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "db_", ".sql");

        assertEquals("db_0__init.sql", sqlMigrationResolver.extractScriptName(new ClassPathResource("db/migration/db_0__init.sql")));
    }

    @Test
    public void extractScriptNameRootLocation() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver("", PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "db_", ".sql");

        assertEquals("db_0__init.sql", sqlMigrationResolver.extractScriptName(new ClassPathResource("db_0__init.sql")));
    }
}
