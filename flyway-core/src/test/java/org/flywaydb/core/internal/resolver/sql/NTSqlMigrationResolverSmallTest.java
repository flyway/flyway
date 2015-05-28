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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testcase for Non Transactional SqlMigration.
 */
public class NTSqlMigrationResolverSmallTest {
    @Test
    public void resolveMigrations() {
        NTSqlMigrationResolver ntSqlMigrationResolver =
                new NTSqlMigrationResolver(null, Thread.currentThread().getContextClassLoader(),
                        new Location("migration/notxnsql"), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "NTV", "__", ".sql");
        Collection<ResolvedMigration> migrations = ntSqlMigrationResolver.resolveMigrations();

        assertEquals(2, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("1.1", migrationList.get(1).getVersion().toString());

        assertEquals("NTV1__Create_colors_type.sql", migrationList.get(0).getScript());
        assertEquals("NTV1_1__Update_colors_type.sql", migrationList.get(1).getScript());
    }
}
