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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;

/**
 * Adapter for executing migrations implementing SpringJdbcMigration.
 */
public class SpringJdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The SpringJdbcMigration to execute.
     */
    private final SpringJdbcMigration springJdbcMigration;

    /**
     * Creates a new SpringJdbcMigrationExecutor.
     *
     * @param springJdbcMigration The Spring Jdbc Migration to execute.
     */
    SpringJdbcMigrationExecutor(SpringJdbcMigration springJdbcMigration) {
        this.springJdbcMigration = springJdbcMigration;
    }

    @Override
    public void execute(Connection connection) {
        try {
            springJdbcMigration.migrate(new org.springframework.jdbc.core.JdbcTemplate(
                    new SingleConnectionDataSource(connection, true)));
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}
