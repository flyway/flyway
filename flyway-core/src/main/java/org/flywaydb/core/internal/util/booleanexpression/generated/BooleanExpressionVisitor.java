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
// Generated from org/flywaydb/core/internal/util/booleanexpression/generated/BooleanExpression.g4 by ANTLR 4.13.2
package org.flywaydb.core.internal.util.booleanexpression.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BooleanExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BooleanExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BooleanExpressionParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(BooleanExpressionParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by the {@code leftWordcomparatorExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftWordcomparatorExpression(BooleanExpressionParser.LeftWordcomparatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code wordcomparatorExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWordcomparatorExpression(BooleanExpressionParser.WordcomparatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code boolExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolExpression(BooleanExpressionParser.BoolExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalOperatorExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperatorExpression(BooleanExpressionParser.LogicalOperatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rightWordcomparatorExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRightWordcomparatorExpression(BooleanExpressionParser.RightWordcomparatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpression(BooleanExpressionParser.ParenExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparatorExpression}
	 * labeled alternative in {@link BooleanExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparatorExpression(BooleanExpressionParser.ComparatorExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanExpressionParser#comparator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparator(BooleanExpressionParser.ComparatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanExpressionParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(BooleanExpressionParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BooleanExpressionParser#bool}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(BooleanExpressionParser.BoolContext ctx);
}
