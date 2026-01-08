/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
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
     * Optionally, a script may provide the expression string describing the conditions under which {@code shouldExecute}
     * returns true. The {@code shouldExecute} function itself is the source of truth for whether a script may execute.
     * This function is provided for providing additional information rather than direct evaluation.
     *
     * @return A string expression for the shouldExecute function, or {@code null}
     */
    default String shouldExecuteExpression() { return null; }

    /**
     * Whether the script should honor placeholders.
     *
     * @return {@code true} if the script should honor placeholders, or {@code false} if not.
     */
    boolean placeholderReplacement();

    /**
     * Validates this SQL script.
     */
    void validate();
}
