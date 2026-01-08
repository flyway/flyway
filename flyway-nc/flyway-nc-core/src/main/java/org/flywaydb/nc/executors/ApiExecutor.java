/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.executors;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.nc.ConnectionType;
import org.flywaydb.core.internal.nc.Executor;
import org.flywaydb.nc.NativeConnectorsNonJdbc;

public class ApiExecutor implements Executor<NonJdbcExecutorExecutionUnit, NativeConnectorsNonJdbc> {

    @Override
    public void execute(final NativeConnectorsNonJdbc experimentalDatabase,
        final NonJdbcExecutorExecutionUnit executionUnit,
        final Configuration configuration) {
        
        if (configuration.isBatch() && !experimentalDatabase.supportsBatch()) {
            throw new FlywayException("The Batch feature is not supported for " + experimentalDatabase.getDatabaseType());
        }

        if (configuration.isBatch() ||
            (experimentalDatabase.transactionAsBatch() && executionUnit.isExecuteInTransaction())) {
            experimentalDatabase.addToBatch(executionUnit);
        } else {
            // There may be previously added scripts pending execution; flush them out.
            if (experimentalDatabase.transactionAsBatch()) {
                experimentalDatabase.doExecuteBatch();
            }
            experimentalDatabase.doExecute(executionUnit, configuration.isOutputQueryResults());
        }
    }

    @Override
    public void finishExecution(final NativeConnectorsNonJdbc experimentalDatabase, final Configuration configuration) {
        if (configuration.isBatch() || experimentalDatabase.transactionAsBatch()) {
            experimentalDatabase.doExecuteBatch();
        }
    }

    @Override
    public boolean canExecute(final ConnectionType connectionType) {
        return connectionType == ConnectionType.API;
    }

    @Override
    public void appendErrorMessage(final NonJdbcExecutorExecutionUnit executionUnit,
        final StringBuilder messageBuilder,
        final boolean isDebugEnabled) {

    }
}
