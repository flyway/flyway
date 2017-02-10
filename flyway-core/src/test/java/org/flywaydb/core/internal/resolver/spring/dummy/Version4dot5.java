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
package org.flywaydb.core.internal.resolver.spring.dummy;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;

import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Test migration.
 */
public class Version4dot5 extends DummyAbstractSpringMongoMigration implements MigrationInfoProvider, MigrationChecksumProvider {

	  @Override
    public void doMigrate(MongoTemplate mongoTemplate) throws Exception {
        //Do nothing
    }

    public Integer getChecksum() {
        return 45;
    }

    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("4.5");
    }

    public String getDescription() {
        return "Four Dot Five";
    }
}
