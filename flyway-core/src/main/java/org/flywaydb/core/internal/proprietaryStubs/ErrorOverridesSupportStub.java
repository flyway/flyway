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
package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ErrorOverridesSupport;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.FlywaySqlScriptException;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

@CustomLog
public class ErrorOverridesSupportStub implements ErrorOverridesSupport {

    @Override
    public void handleException(final Results results,
        final SqlScript sqlScript,
        final SqlStatement sqlStatement,
        final Configuration config) {
        throw new FlywaySqlScriptException(sqlScript.getResource(),
            sqlStatement,
            results.getException(),
            config.getCurrentEnvironmentName());
    }

    @Override
    public void printWarnings(final Results results, final Configuration config) {
        for (final Warning warning : results.getWarnings()) {
            printWarning(warning);
        }
    }

    @Override
    public int getPriority() {
        return -100;
    }

    protected void printWarning(final Warning warning) {
        if ("00000".equals(warning.getState())) {
            LOG.info("DB: " + warning.getMessage());
        } else {
            LOG.warn("DB: "
                + warning.getMessage()
                + " (SQL State: "
                + warning.getState()
                + " - Error Code: "
                + warning.getCode()
                + ")");
        }
    }
}
