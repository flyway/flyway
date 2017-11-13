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
    public Integer getChecksum() {
        return ++count;
    }
}
