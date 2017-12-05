/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.resolver.jdbc.dummy;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Trips up the ClassPathScanner. See issue 801.
 */
@SuppressWarnings("UnusedDeclaration")
public enum SabotageEnum implements JdbcMigration {
    FAIL {
        @Override
        public void migrate(Connection connection) throws Exception {

        }
    }
}
