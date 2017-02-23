/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
