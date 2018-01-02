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
package org.flywaydb.core.internal.resolver.spring.dummy;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test migration.
 */
public class Version3dot5 extends DummyAbstractSpringJdbcMigration implements MigrationInfoProvider, MigrationChecksumProvider {
    public void doMigrate(JdbcTemplate jdbcTemplate) throws Exception {
        //Do nothing
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
