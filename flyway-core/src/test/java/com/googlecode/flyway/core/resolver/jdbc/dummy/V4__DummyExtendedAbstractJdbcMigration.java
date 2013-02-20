package com.googlecode.flyway.core.resolver.jdbc.dummy;

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
