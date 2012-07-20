package com.googlecode.flyway.core.migration.spring.dummy;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test for abstract class support.
 */
public abstract class DummyAbstractSpringJdbcMigration implements SpringJdbcMigration {
    public final void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        doMigrate(jdbcTemplate);
    }

    public abstract void doMigrate(JdbcTemplate jdbcTemplate) throws Exception;
}
