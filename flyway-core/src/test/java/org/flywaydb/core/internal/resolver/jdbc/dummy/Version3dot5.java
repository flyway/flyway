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

    @Override
    public boolean isUndo() {
        return false;
    }
}
