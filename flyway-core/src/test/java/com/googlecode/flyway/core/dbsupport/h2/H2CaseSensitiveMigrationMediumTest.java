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
package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.junit.Test;

/**
 * Test for H2 in case-sensitive (DATABASE_TO_UPPER=FALSE) mode.
 */
public class H2CaseSensitiveMigrationMediumTest {
    @Test
    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(new DriverDataSource(null, "jdbc:h2:mem:flyway_db_case_sensitive;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", "sa", ""));
        flyway.setLocations("migration/sql");
        flyway.migrate();
        flyway.clean();
    }
}
