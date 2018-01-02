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

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
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
