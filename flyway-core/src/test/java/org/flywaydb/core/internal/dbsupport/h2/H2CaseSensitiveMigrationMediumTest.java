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
package org.flywaydb.core.internal.dbsupport.h2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test for H2 in case-sensitive (DATABASE_TO_UPPER=FALSE) mode.
 */
@Category(DbCategory.H2.class)
public class H2CaseSensitiveMigrationMediumTest {
    @Test
    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_db_case_sensitive;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", "sa", "");
        flyway.setLocations("migration/sql");
        flyway.migrate();
        flyway.clean();
    }
}
