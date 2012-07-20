package com.googlecode.flyway.core.migration.jdbc.dummy;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

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
