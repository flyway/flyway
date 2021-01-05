/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;

public class BooleanEvaluator {
    /**
     * Evaluates a boolean expression.
     *
     * Currently only supports expressions that are 'true', 'false', or 'A==B'
     * where A and B are values and not themselves expressions.
     *
     * @param expression The string containing the boolean expression.
     * @return           The boolean value the expression evaluates to.
     */
    public static boolean evaluateExpression(String expression) {
        if (expression.trim().equalsIgnoreCase("true")) {
            return true;
        } else if (expression.trim().equalsIgnoreCase("false")) {
            return false;
        } else {
            String[] operands = expression.split("==");
            if (operands.length != 2) {
                throw new FlywayException("Invalid boolean expression '" + expression + "'. " +
                        "Expression should be 'true', 'false', or 'A==B' where A and B are values and not themselves expressions.");
            }
            return operands[0].trim().equalsIgnoreCase(operands[1].trim());
        }
    }
}