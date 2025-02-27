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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.ExperimentalDatabase;

public class NonJdbcExecutor implements Executor<String> {

    @Override
    public void execute(final ExperimentalDatabase experimentalDatabase,
        final String executionUnit,
        final Configuration configuration) {
        experimentalDatabase.doExecute(executionUnit, configuration.isOutputQueryResults());
    }

    @Override
    public void finishExecution(final ExperimentalDatabase experimentalDatabase, final Configuration configuration) {

    }

    @Override
    public boolean canExecute(final ConnectionType connectionType) {
        return connectionType == ConnectionType.EXECUTABLE || connectionType == ConnectionType.API;
    }

    @Override
    public void appendErrorMessage(final String executionUnit,
        final StringBuilder messageBuilder,
        final boolean isDebugEnabled) {

    }
}
