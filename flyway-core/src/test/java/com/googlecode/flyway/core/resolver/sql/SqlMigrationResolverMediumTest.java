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
package com.googlecode.flyway.core.resolver.sql;

import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.util.Location;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import org.junit.Test;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
                new SqlMigrationResolver(new Location("filesystem:" + path), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", ".sql");
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
}
