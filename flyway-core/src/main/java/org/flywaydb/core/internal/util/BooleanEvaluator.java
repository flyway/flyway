/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.sqlscript.ShouldExecuteEvaluator;

public class BooleanEvaluator {
    /**
     * Evaluates a boolean expression.
     * <p>
     * Currently only supports expressions that are 'true', 'false', 'A==B', 'A!=B' and combinations of those using ( )
     * (precedence), && (AND), || (OR)
     *
     * @param expression The string containing the boolean expression.
     * @return The boolean value the expression evaluates to.
     */
    public static boolean evaluateExpression(final String expression, final Configuration configuration) {
        final ShouldExecuteEvaluator plugin = configuration.getPluginRegister()
            .getLicensedPlugin(ShouldExecuteEvaluator.class, configuration);
        return plugin == null || plugin.evaluateExpression(expression);
    }
}
