/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
