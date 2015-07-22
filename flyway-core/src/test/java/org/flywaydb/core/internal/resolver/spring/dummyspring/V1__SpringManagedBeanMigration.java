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
package org.flywaydb.core.internal.resolver.spring.dummyspring;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * This is a Spring managed bean migration to test if the dependency injection of a {@link DummyBean} into
 * a migration is working.
 */
@Component
public class V1__SpringManagedBeanMigration implements SpringJdbcMigration, MigrationInfoProvider {

    private final DummyBean dummyBean;

    @Autowired
    public V1__SpringManagedBeanMigration(DummyBean dummyBean) {
        this.dummyBean = dummyBean;
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        // do nothing
    }

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("1.0");
    }

    @Override
    public String getDescription() {
        // return the dummy bean property to get it in the test class
        return this.dummyBean.getProperty();
    }
}
