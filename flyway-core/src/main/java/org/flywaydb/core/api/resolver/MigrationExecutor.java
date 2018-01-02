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
package org.flywaydb.core.api.resolver;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Executes a migration.
 */
public interface MigrationExecutor {
    /**
     * Executes the migration this executor is associated with.
     *
     * @param connection The connection to use to execute the migration against the DB.
     * @throws SQLException when the execution of a statement failed.
     */
    void execute(Connection connection) throws SQLException;

    /**
     * Whether the execution should take place inside a transaction. Almost all implementation should return {@code true}.
     * This however makes it possible to execute certain migrations outside a transaction. This is useful for databases
     * like PostgreSQL where certain statement can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    boolean executeInTransaction();
}
