/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public interface SqlStatement {
    /**
     * @return The original line number where the statement was located in the script it came from.
     */
    int getLineNumber();

    /**
     * @return The sql to send to the database.
     */
    String getSql();

    /**
     * @return The delimiter for the statement.
     */
    String getDelimiter();

    /**
     * Whether the execution should take place inside a transaction. Almost all implementation should return {@code true}.
     * This however makes it possible to execute certain migrations outside a transaction. This is useful for databases
     * like PostgreSQL and SQL Server where certain statement can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    boolean canExecuteInTransaction();

    /**
     * @return Whether this statement can be run as part of batch or whether it must be run individually.
     */
    boolean isBatchable();









    /**
     * Executes this statement against the database.
     *
     * @param jdbcTemplate The jdbcTemplate to use to execute this script.
     * @return the result of the execution.
     */
    Results execute(JdbcTemplate jdbcTemplate, SqlScriptExecutor sqlScriptExecutor, Configuration config);
}
