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
package org.flywaydb.core.internal.resolver.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;

import java.sql.Connection;

/**
 * Adapter for executing migrations implementing JdbcMigration.
 */
public class JdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The JdbcMigration to execute.
     */
    private final JdbcMigration jdbcMigration;

    /**
     * Creates a new JdbcMigrationExecutor.
     *
     * @param jdbcMigration The JdbcMigration to execute.
     */
    JdbcMigrationExecutor(JdbcMigration jdbcMigration) {
        this.jdbcMigration = jdbcMigration;
    }

    @Override
    public void execute(Connection connection) {
        try {
            jdbcMigration.migrate(connection);
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}
