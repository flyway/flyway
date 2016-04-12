/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.jdbc.dummy;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;

import java.sql.Connection;

/**
 * Test migration.
 */
public class Version3dot5 extends DummyAbstractJdbcMigration implements MigrationInfoProvider, MigrationChecksumProvider {
    public void doMigrate(Connection connection) throws Exception {
        //Do nothing.
    }

    public Integer getChecksum() {
        return 35;
    }

    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("3.5");
    }

    public String getDescription() {
        return "Three Dot Five";
    }
}
