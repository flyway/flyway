/*-
 * ========================LICENSE_START=================================
 * flyway-verb-utils
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.verb.executors;

import static org.flywaydb.core.internal.sqlscript.FlywaySqlScriptException.STATEMENT_MESSAGE;

import lombok.CustomLog;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

@CustomLog
public class JdbcExecutor implements Executor<SqlStatement> {

    @Override
    public void execute(final ExperimentalDatabase experimentalDatabase,
        final SqlStatement executionUnit,
        final Configuration configuration) {
        if (configuration.isBatch()) {
            if (executionUnit.isBatchable()) {
                experimentalDatabase.addToBatch(executionUnit.getSql());
                if (experimentalDatabase.getBatchSize() >= 100) {
                    experimentalDatabase.doExecuteBatch();
                }
            } else {
                experimentalDatabase.doExecuteBatch();
                experimentalDatabase.doExecute(executionUnit.getSql(), configuration.isOutputQueryResults());
            }
        } else {
            experimentalDatabase.doExecute(executionUnit.getSql(), configuration.isOutputQueryResults());
        }
    }

    @Override
    public void finishExecution(final ExperimentalDatabase experimentalDatabase, final Configuration configuration) {
        if (configuration.isBatch()) {
            experimentalDatabase.doExecuteBatch();
        }
    }

    @Override
    public boolean canExecute(final ConnectionType connectionType) {
        return connectionType == ConnectionType.JDBC;
    }

    @Override
    public void appendErrorMessage(final SqlStatement executionUnit,
        final StringBuilder messageBuilder,
        final boolean isDebugEnabled) {
        messageBuilder.append("Line       : ").append(executionUnit.getLineNumber()).append("\n");
        messageBuilder.append("Statement  : ")
            .append(isDebugEnabled ? executionUnit.getSql() : STATEMENT_MESSAGE)
            .append("\n");
    }
}
