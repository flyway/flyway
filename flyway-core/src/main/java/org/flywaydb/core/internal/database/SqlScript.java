/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.util.List;

public abstract class SqlScript<C extends ContextImpl> {
    /**
     * The resource containing the statements.
     */
    protected final LoadableResource resource;

    /**
     * The placeholder replacer.
     */
    protected final PlaceholderReplacer placeholderReplacer;

    /**
     * Creates a new sql script from this source.
     *
     * @param resource            The sql script resource.
     * @param placeholderReplacer The placeholder replacer.
     */
    protected SqlScript(LoadableResource resource, PlaceholderReplacer placeholderReplacer) {
        this.resource = resource;
        this.placeholderReplacer = placeholderReplacer;
    }

    /**
     * For increased testability.
     *
     * @return The sql statements contained in this script.
     */
    public abstract List<SqlStatement<C>> getSqlStatements();

    /**
     * @return The resource containing the statements.
     */
    public final LoadableResource getResource() {
        return resource;
    }

    /**
     * Whether the execution should take place inside a transaction. This is useful for databases
     * like PostgreSQL where certain statement can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    public abstract boolean executeInTransaction();

    /**
     * Executes this script against the database.
     *
     * @param jdbcTemplate The jdbcTemplate to use to execute this script.
     */
    public abstract void execute(JdbcTemplate jdbcTemplate);
}