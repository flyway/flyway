/*
 * Copyright 2010-2018 Boxfuse GmbH
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
