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

import java.sql.Connection;

/**
 * Test class that extends and abstract class instead of implementing JdbcMigration directly.
 */
public class V4__DummyExtendedAbstractJdbcMigration extends DummyAbstractJdbcMigration {
    @Override
    public void doMigrate(Connection connection) throws Exception {
        // DO nothing
    }
}
