/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.database.bigquery;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BigQueryParser extends Parser {
    private static final String ALTERNATIVE_SINGLE_LINE_COMMENT = "#";
    private static final String TRIPLE_STRING_LITERAL_SINGLE_QUOTE = "'''";
    private static final String TRIPLE_STRING_LITERAL_DOUBLE_QUOTE = "\"\"\"";

    public BigQueryParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected Set<String> getValidKeywords() {
        // https://cloud.google.com/bigquery/docs/reference/standard-sql/lexical#reserved_keywords
        return new HashSet<>(Arrays.asList(
                "ALL", "AND", "ANY", "ARRAY", "AS", "ASC", "ASSERT_ROWS_MODIFIED", "AT",
                "BEGIN", "BETWEEN", "BY",
                "CASE", "CAST", "COLLATE", "CONTAINS", "CREATE", "CROSS", "CUBE", "CURRENT",
                "DEFAULT", "DEFINE", "DESC", "DISTINCT",
                "ELSE", "END", "ENUM", "ESCAPE", "EXCEPT", "EXCLUDE", "EXISTS", "EXTRACT",
                "FALSE", "FETCH", "FOLLOWING", "FOR", "FROM", "FULL",
                "GROUP", "GROUPING", "GROUPS",
                "HASH", "HAVING",
                "IF", "IGNORE", "IN", "INNER",
                "INTERSECT", "INTERVAL", "INTO", "IS",
                "JOIN",
                "LATERAL", "LEFT", "LIKE", "LIMIT", "LOOKUP", "LOOP",
                "MERGE",
                "NATURAL", "NEW", "NO",
                "NOT", "NULL", "NULLS",
                "OF", "ON", "OR",
                "ORDER", "OUTER", "OVER",
                "PARTITION", "PRECEDING", "PROTO",
                "RANGE", "RECURSIVE", "RESPECT", "RIGHT", "ROLLUP", "ROWS",
                "SELECT", "SET", "SOME", "STRUCT",
                "TABLESAMPLE", "THEN", "TO", "TRANSACTION", "TREAT", "TRUE",
                "UNBOUNDED", "UNION", "UNNEST", "USING",
                "WHEN", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN"
                                          ));
    }

    @Override
    protected char getIdentifierQuote() {
        return '`';
    }

    @Override
    protected char getAlternativeStringLiteralQuote() {
        return '"';
    }

    @Override
    protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
        return super.isSingleLineComment(peek, context, col)
                || peek.startsWith(ALTERNATIVE_SINGLE_LINE_COMMENT);
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
        return false;
    }

    @Override
    protected Token handleStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        // BigQuery also supports ''' to quote string.
        handleAmbiguityStringLiteral(reader, '\'', TRIPLE_STRING_LITERAL_SINGLE_QUOTE);
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        // BigQuery also supports """ to quote string.
        handleAmbiguityStringLiteral(reader, '"', TRIPLE_STRING_LITERAL_DOUBLE_QUOTE);
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    private void handleAmbiguityStringLiteral(PeekingReader reader, char singleQuote, String tripleQuote) throws IOException {
        if (reader.peek(tripleQuote)) {
            reader.swallow(tripleQuote.length());
            reader.swallowUntilExcluding(tripleQuote);
            reader.swallow(tripleQuote.length());
        } else {
            reader.swallow();
            reader.swallowUntilIncludingWithEscape(singleQuote, false, '\\');
        }
    }

    @Override
    protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
        TokenType tokenType = token.getType();
        if (TokenType.EOF.equals(tokenType) || TokenType.DELIMITER.equals(tokenType) || ";".equals(token.getText())) {
            return true;
        }

        Token lastToken = getPreviousToken(tokens, context.getParensDepth());
        if (lastToken != null && lastToken.getType() == TokenType.KEYWORD) {
            return true;
        }

        return super.shouldAdjustBlockDepth(context, tokens, token);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
        String keywordText = keyword.getText();
        int parensDepth = keyword.getParensDepth();

        if ("BEGIN".equalsIgnoreCase(keywordText)) {
            context.increaseBlockDepth(keywordText);
        }

        if (lastTokenIs(tokens, parensDepth, "BEGIN") &&
                ("TRANSACTION".equalsIgnoreCase(keywordText) || ";".equalsIgnoreCase(keywordText))
                && context.getBlockDepth() > 0) {
            context.decreaseBlockDepth();
        }

        if (lastTokenIs(tokens, parensDepth, "END") &&
                !"IF".equalsIgnoreCase(keywordText) && !"WHILE".equalsIgnoreCase(keywordText) && !"LOOP".equalsIgnoreCase(keywordText)
                && context.getBlockDepth() > 0) {
            context.decreaseBlockDepth();
        }
    }
}