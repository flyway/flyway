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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.FlywayConfigurationForTests;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.junit.Test;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Medium test for SqlMigrationResolver.
 */
public class SqlMigrationResolverMediumTest {

    @Test
    public void resolveMigrations() throws Exception {
        @SuppressWarnings("ConstantConditions")
        String path = URLDecoder.decode(getClass().getClassLoader().getResource("migration/subdir").getPath(), "UTF-8");

        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, new Scanner(Thread.currentThread().getContextClassLoader()),
                        new Locations("filesystem:" + new File(path).getPath()), PlaceholderReplacer.NO_PLACEHOLDERS,
                        FlywayConfigurationForTests.create());
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
}
