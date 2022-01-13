/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
     * Currently only supports expressions that are 'true', 'false', 'A==B', 'A!=B'
     * and combinations of those using ( ) (precedence), && (AND), || (OR)
     *
     * @param expression The string containing the boolean expression.
     * @return The boolean value the expression evaluates to.
     */
    public static boolean evaluateExpression(String expression) {
        while (expression.contains("(")) {
            String innermost = findInnermostBrackets(expression);
            expression = expression.replace("(" + innermost + ")", evaluateExpression(innermost) ? "true" : "false");
        }

        if (expression.trim().equalsIgnoreCase("true")) {
            return true;
        } else if (expression.trim().equalsIgnoreCase("false")) {
            return false;
        } else if (expression.contains("&&")) {
            String[] expressions = expression.split("&&");
            return andAll(expressions);
        } else if (expression.contains("||")) {
            String[] expressions = expression.split("\\|\\|");
            return orAll(expressions);
        } else if (expression.contains("!=")) {
            return !compareOperands("!=", expression);
        } else if (expression.contains("==")) {
            return compareOperands("==", expression);
        } else {
            throw new FlywayException("Unable to parse expression: " + expression);
        }
    }

    private static String findInnermostBrackets(String expression) {
        int openIndex = -1;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') {
                openIndex = i + 1;
            }
            if (expression.charAt(i) == ')') {
                if (openIndex == -1) {
                    throw new FlywayException("Unable to parse expression: " + expression + " - unexpected )");
                }
                return expression.substring(openIndex, i);
            }
        }
        throw new FlywayException("Unable to parse expression: " + expression + " - unexpected (");
    }

    private static boolean compareOperands(String operator, String expression) {
        String[] operands = expression.split(operator);
        if (operands.length != 2) {
            throw new FlywayException("Invalid boolean expression '" + expression + "'. ");
        }
        return operands[0].trim().equals(operands[1].trim());
    }

    private static boolean andAll(String[] expressions) {
        for (String expression : expressions) {
            if (!evaluateExpression(expression)) {
                return false;
            }
        }
        return true;
    }

    private static boolean orAll(String[] expressions) {
        for (String expression : expressions) {
            if (evaluateExpression(expression)) {
                return true;
            }
        }
        return false;
    }
}