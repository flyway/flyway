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
package org.flywaydb.core.internal.resolver.jdbc.error;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Test for exception in constructor support.
 */
public class BrokenJdbcMigration implements JdbcMigration {
    public BrokenJdbcMigration() {
        throw new IllegalStateException("Expected!");
    }

    public final void migrate(Connection connection) throws Exception {
        // Do nothing
    }
}
