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
package org.flywaydb.core.internal.repeatable;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

@SuppressWarnings("ALL")
public class R_Migration implements JdbcMigration, MigrationInfoProvider, MigrationChecksumProvider {
    private static int count;

    @Override
    public void migrate(final Connection c) throws Exception {
        System.err.println("I am running");
    }

    @Override
    public MigrationVersion getVersion() {
        return null;
    }

    @Override
    public String getDescription() {
        return "R_Migration";
    }

    @Override
    public boolean isUndo() {
        return false;
    }

    @Override
    public Integer getChecksum() {
        return ++count;
    }
}
