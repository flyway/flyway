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
package org.flywaydb.core.internal.resolver.jdbc.dummy;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Test for abstract class support.
 */
public abstract class DummyAbstractJdbcMigration implements JdbcMigration {
    public final void migrate(Connection connection) throws Exception {
        doMigrate(connection);
    }

    public abstract void doMigrate(Connection connection) throws Exception;
}
