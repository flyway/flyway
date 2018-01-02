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
