package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.resource.LoadableResource;

import java.util.Collection;

/**
 * SQL script containing a series of statements terminated by a delimiter (eg: ;).
 * Single-line (--) and multi-line (/* * /) comments are stripped and ignored.
 */
public interface SqlScript extends Comparable<SqlScript> {
    /**
     * @return The sql statements contained in this script.
     */
    SqlStatementIterator getSqlStatements();

    /**
     * @return The number of sql statements contained in this script.
     */
    int getSqlStatementCount();

    /**
     * @return The external SQL scripts referenced directly or indirectly by this SQL script.
     */
    Collection<SqlScript> getReferencedSqlScripts();

    /**
     * @return The resource containing the statements.
     */
    LoadableResource getResource();

    /**
     * Whether the execution should take place inside a transaction. This is useful for databases
     * like PostgreSQL where certain statement can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    boolean executeInTransaction();

    /**
     * Whether the script should execute or not.
     *
     * @return {@code true} if the script should execute, or {@code false} if not.
     */
    boolean shouldExecute();

    /**
     * Validates this SQL script.
     */
    void validate();
}