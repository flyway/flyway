/**
 * Copyright 2010-2015 Axel Fontaine
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
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for SqlMigration.
 */
public class SqlMigrationResolverCRTest {
    @Test
    public void resolveMigrations() {
        SqlMigrationResolver sqlMigrationResolver =
                new SqlMigrationResolver(null, Thread.currentThread().getContextClassLoader(),
                        new Location("migration/cr"), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", "__", ".txt");
        Collection<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(2, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("2", migrationList.get(1).getVersion().toString());

        assertEquals(migrationList.get(0).getChecksum(), migrationList.get(1).getChecksum());
    }

}
