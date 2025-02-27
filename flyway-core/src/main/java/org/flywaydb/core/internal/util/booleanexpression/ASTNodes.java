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

import static lombok.AccessLevel.PACKAGE;

import lombok.Data;
import lombok.RequiredArgsConstructor;

class ASTNodes {

    private ASTNodes() {}

    public interface Expression {}

    @Data
    @RequiredArgsConstructor(access = PACKAGE)
    static class InfixExpression<T> implements Expression {
        private final Expression left;
        private final Expression right;
        private final T operator;
    }

    static class ComparisonExpression extends InfixExpression<Comparator> {
        ComparisonExpression(final Expression left, final Expression right, final Comparator comparator) {
            super(left, right, comparator);
        }
    }

    static class LogicalExpression extends InfixExpression<LogicalOperator> {
        LogicalExpression(final Expression left, final Expression right, final LogicalOperator operator) {
            super(left, right, operator);
        }
    }

    record WordComparisonExpression(WordNode leftWord,
                                    WordNode rightWord,
                                    Comparator comparator) implements Expression {}

    record WordExpressionComparisonExpression(WordNode word, Expression expression, Comparator comparator) implements
                                                                                                           Expression {}

    record BooleanNode(boolean value) implements Expression {}

    record WordNode(String word) {}

    enum Comparator {
        EQUAL,
        NOT_EQUAL
    }

    enum LogicalOperator {
        AND,
        OR
    }
}
