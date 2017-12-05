/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.jdbc;

import java.sql.Savepoint;

/**
 * An exception that triggers a rollback up to this savepoint.
 */
public class RollbackWithSavepointException extends RuntimeException {
    /**
     * Savepoint to rollback to.
     */
    private final Savepoint savepoint;

    /**
     * Creates an exception that triggers a rollback up to this safepoint.
     *
     * @param savepoint Savepoint to rollback to.
     * @param cause     The cause for the rollback.
     */
    public RollbackWithSavepointException(Savepoint savepoint, RuntimeException cause) {
        super(cause);
        this.savepoint = savepoint;
    }

    /**
     * @return Savepoint to rollback to.
     */
    public Savepoint getSavepoint() {
        return savepoint;
    }
}
