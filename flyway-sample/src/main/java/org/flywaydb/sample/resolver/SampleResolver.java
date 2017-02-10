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
package org.flywaydb.sample.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

/**
 * Sample MigrationResolver.
 */
public class SampleResolver implements MigrationResolver {
    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        return Arrays.asList((ResolvedMigration) new ResolvedMigration() {
            @Override
            public MigrationVersion getVersion() {
                return MigrationVersion.fromVersion("2.0");
            }

            @Override
            public String getDescription() {
                return "Custom Resolved";
            }

            @Override
            public String getScript() {
                return "R1";
            }

            @Override
            public Integer getChecksum() {
                return 20;
            }

            @Override
            public MigrationType getType() {
                return MigrationType.CUSTOM;
            }

            @Override
            public String getPhysicalLocation() {
                return null;
            }

            @Override
            public MigrationExecutor getExecutor() {
                return new MigrationExecutor() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        Statement statement = null;
                        try {
                            statement = connection.createStatement();
                            statement.execute("INSERT INTO test_user (name) VALUES ('Resolvix')");
                        } finally {
                            JdbcUtils.closeStatement(statement);
                        }
                    }

                    @Override
                    public boolean executeInTransaction() {
                        return true;
                    }
                };
            }
        });
    }
}
