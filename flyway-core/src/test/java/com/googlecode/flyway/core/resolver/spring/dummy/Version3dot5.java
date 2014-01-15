/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.resolver.spring.dummy;

import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.api.migration.MigrationChecksumProvider;
import com.googlecode.flyway.core.api.migration.MigrationInfoProvider;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test migration.
 */
public class Version3dot5 extends DummyAbstractSpringJdbcMigration implements MigrationInfoProvider, MigrationChecksumProvider {
    public void doMigrate(JdbcTemplate jdbcTemplate) throws Exception {
        //Do nothing
    }

    public Integer getChecksum() {
        return 35;
    }

    public MigrationVersion getVersion() {
        return new MigrationVersion("3.5");
    }

    public String getDescription() {
        return "Three Dot Five";
    }
}
