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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
* Created by Axel on 3/7/14.
*/
public class MyCustomMigrationResolver implements MigrationResolver {
    @Override
    public List<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> resolvedMigrations = new ArrayList<ResolvedMigration>();
        resolvedMigrations.add(new ResolvedMigration() {
            @Override
            public MigrationVersion getVersion() {
                return MigrationVersion.fromVersion("1.9");
            }

            @Override
            public String getDescription() {
                return "Virtual Migration";
            }

            @Override
            public String getScript() {
                return "VirtualScript 1.9";
            }

            @Override
            public Integer getChecksum() {
                return 19;
            }

            @Override
            public MigrationType getType() {
                return MigrationType.CUSTOM;
            }

            @Override
            public String getPhysicalLocation() {
                return "virtual://loaction";
            }

            @Override
            public MigrationExecutor getExecutor() {
                return new MigrationExecutor() {
                    @Override
                    public void execute(Connection connection) {
                        System.out.println("Executed !");
                    }

                    @Override
                    public boolean executeInTransaction() {
                        return true;
                    }
                };
            }
        });
        return resolvedMigrations;
    }
}
