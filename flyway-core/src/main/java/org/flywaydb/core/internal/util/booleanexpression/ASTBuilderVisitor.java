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

import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.BooleanNode;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.Comparator;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.ComparisonExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.Expression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.LogicalExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.LogicalOperator;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.WordComparisonExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.WordExpressionComparisonExpression;
import org.flywaydb.core.internal.util.booleanexpression.ASTNodes.WordNode;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionBaseVisitor;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.BoolContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.BoolExpressionContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.ComparatorContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.ComparatorExpressionContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.LeftWordcomparatorExpressionContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.LogicalOperatorExpressionContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.ParenExpressionContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.ProgramContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.RightWordcomparatorExpressionContext;
import org.flywaydb.core.internal.util.booleanexpression.generated.BooleanExpressionParser.WordcomparatorExpressionContext;

public class ASTBuilderVisitor extends BooleanExpressionBaseVisitor<Expression> {

    @Override
    public Expression visitProgram(final ProgramContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Expression visitParenExpression(final ParenExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Expression visitComparatorExpression(final ComparatorExpressionContext ctx) {
        final var comparator = getComparator(ctx.comparator());
        return new ComparisonExpression(visit(ctx.left), visit(ctx.right), comparator);
    }

    @Override
    public Expression visitLogicalOperatorExpression(final LogicalOperatorExpressionContext ctx) {
        final var operator = ctx.logicalOperator().getStart().getType() == BooleanExpressionParser.AND
            ? LogicalOperator.AND
            : LogicalOperator.OR;

        return new LogicalExpression(visit(ctx.left), visit(ctx.right), operator);
    }

    @Override
    public Expression visitBoolExpression(final BoolExpressionContext ctx) {
        return visit(ctx.bool());
    }

    @Override
    public Expression visitBool(final BoolContext ctx) {
        return new BooleanNode(ctx.getStart().getType() == BooleanExpressionParser.TRUE);
    }

    @Override
    public Expression visitWordcomparatorExpression(final WordcomparatorExpressionContext ctx) {
        final var comparator = getComparator(ctx.comparator());
        final var leftWord = new WordNode(ctx.WORD(0).getText());
        final var rightWord = new WordNode(ctx.WORD(1).getText());

        return new WordComparisonExpression(leftWord, rightWord, comparator);
    }

    @Override
    public Expression visitLeftWordcomparatorExpression(final LeftWordcomparatorExpressionContext ctx) {
        final var comparator = getComparator(ctx.comparator());
        final var word = new WordNode(ctx.WORD().getText());

        return new WordExpressionComparisonExpression(word, visit(ctx.expression()), comparator);
    }

    @Override
    public Expression visitRightWordcomparatorExpression(final RightWordcomparatorExpressionContext ctx) {
        final var comparator = getComparator(ctx.comparator());
        final var word = new WordNode(ctx.WORD().getText());

        return new WordExpressionComparisonExpression(word, visit(ctx.expression()), comparator);
    }

    private static Comparator getComparator(final ComparatorContext ctx) {
        return ctx.getStart().getType() == BooleanExpressionParser.EQUAL ? Comparator.EQUAL : Comparator.NOT_EQUAL;
    }
}
