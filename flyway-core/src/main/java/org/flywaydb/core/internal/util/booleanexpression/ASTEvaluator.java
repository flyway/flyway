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
package org.flywaydb.core.internal.util.booleanexpression;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.BooleanNode;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.ComparisonExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.Expression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.LogicalExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.WordComparisonExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.WordExpressionComparisonExpression;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ASTEvaluator {

    private static final Map<Class<? extends Expression>, Function<Expression, Boolean>> EVALUATORS = Map.of(
        ComparisonExpression.class,
        e -> evaluate((ComparisonExpression) e),
        WordComparisonExpression.class,
        e -> evaluate((WordComparisonExpression) e),
        WordExpressionComparisonExpression.class,
        e -> evaluate((WordExpressionComparisonExpression) e),
        LogicalExpression.class,
        e -> evaluate((LogicalExpression) e),
        BooleanNode.class,
        e -> evaluate((BooleanNode) e));

    private static boolean evaluate(final ComparisonExpression expression) {
        return switch (expression.getOperator()) {
            case EQUAL -> evaluate(expression.getLeft()) == evaluate(expression.getRight());
            case NOT_EQUAL -> evaluate(expression.getLeft()) != evaluate(expression.getRight());
        };
    }

    private static boolean evaluate(final WordComparisonExpression expression) {
        final var areEqual = Objects.equals(expression.leftWord().word(), expression.rightWord().word());
        return switch (expression.comparator()) {
            case EQUAL -> areEqual;
            case NOT_EQUAL -> !areEqual;
        };
    }

    private static boolean evaluate(final WordExpressionComparisonExpression expression) {
        final var expressionValue = Boolean.toString(evaluate(expression.expression()));
        final var wordValue = expression.word().word().toLowerCase(Locale.ROOT);
        final var areEqual = Objects.equals(expressionValue, wordValue);
        return switch (expression.comparator()) {
            case EQUAL -> areEqual;
            case NOT_EQUAL -> !areEqual;
        };
    }

    private static boolean evaluate(final LogicalExpression expression) {
        return switch (expression.getOperator()) {
            case AND -> evaluate(expression.getLeft()) && evaluate(expression.getRight());
            case OR -> evaluate(expression.getLeft()) || evaluate(expression.getRight());
        };
    }

    private static boolean evaluate(final BooleanNode expression) {
        return expression.value();
    }

    public static boolean evaluate(final Expression expression) {
        return Optional.ofNullable(EVALUATORS.get(expression.getClass())).orElseThrow(() -> new FlywayException(
            "No matching expression evaluator found for class: " + expression.getClass(),
            CoreErrorCode.FAULT)).apply(expression);
    }
}
