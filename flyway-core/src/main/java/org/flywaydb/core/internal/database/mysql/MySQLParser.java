/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MySQLParser extends Parser {
    private static final char ALTERNATIVE_SINGLE_LINE_COMMENT = '#';
    private IfState ifState;
    private String previousKeywordText;

    enum IfState {
        NONE,
        IF_FUNCTION,
        IF_NOT,
        IF_EXISTS,
        IF,
        IF_THEN,
        UNKNOWN
    }

    public MySQLParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 8);
        ifState = IfState.NONE;
        previousKeywordText = "";
    }

    @Override
    protected void resetDelimiter(ParserContext context) {
        // Do not reset delimiter as delimiter changes survive beyond a single statement
    }

    @Override
    protected Token handleKeyword(PeekingReader reader, ParserContext context, int pos, int line, int col, String keyword) throws IOException {
        if (keywordIs("DELIMITER", keyword)) {
            String text = reader.readUntilExcluding('\n', '\r').trim();
            return new Token(TokenType.NEW_DELIMITER, pos, line, col, text, text, context.getParensDepth());
        }
        return super.handleKeyword(reader, context, pos, line, col, keyword);
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
        return (super.isSingleLineComment(peek, context, col)
                // Normally MySQL treats # as a comment, but this may have been overridden by DELIMITER # directive
                || (peek.charAt(0) == ALTERNATIVE_SINGLE_LINE_COMMENT && !isDelimiter(peek, context, col)));
    }

    @Override
    protected Token handleStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow();
        reader.swallowUntilExcludingWithEscape('\'', true, '\\');
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow();
        reader.swallowUntilExcludingWithEscape('"', true, '\\');
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    @Override
    protected Token handleCommentDirective(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow(2);
        String text = reader.readUntilExcluding("*/");
        reader.swallow(2);
        return new Token(TokenType.MULTI_LINE_COMMENT_DIRECTIVE, pos, line, col, text, text, context.getParensDepth());
    }

    @Override
    protected boolean isCommentDirective(String text) {
        return text.length() >= 8
                && text.charAt(0) == '/'
                && text.charAt(1) == '*'
                && text.charAt(2) == '!'
                && isDigit(text.charAt(3))
                && isDigit(text.charAt(4))
                && isDigit(text.charAt(5))
                && isDigit(text.charAt(6))
                && isDigit(text.charAt(7));
    }

    @Override
    protected boolean shouldAdjustBlockDepth(ParserContext context, Token token) {
        TokenType tokenType = token.getType();
        if (TokenType.DELIMITER.equals(tokenType) || ";".equals(token.getText())) {
            return true;
        } else if (TokenType.EOF.equals(tokenType)) {
            return true;
        }

        return super.shouldAdjustBlockDepth(context, token);
    }

    // These words increase the block depth - unless preceded by END (in which case the END will decrease the block depth)
    // See: https://dev.mysql.com/doc/refman/8.0/en/flow-control-statements.html
    private static final List<String> CONTROL_FLOW_KEYWORDS = Arrays.asList("LOOP", "CASE", "REPEAT", "WHILE");

    private static final Pattern CREATE_IF_NOT_EXISTS = Pattern.compile(
            ".*CREATE\\s([^\\s]+\\s){1,2}IF\\sNOT\\sEXISTS");
    private static final Pattern DROP_IF_EXISTS = Pattern.compile(
            ".*DROP\\s([^\\s]+\\s){1,2}IF\\sEXISTS");

    private boolean doesDelimiterEndFunction(List<Token> tokens, Token delimiter) {

        // if there's not enough tokens, its not the function
        if (tokens.size() < 2) {
            return false;
        }

        // if the previous keyword was not inside some brackets, it's not the function
        if (tokens.get(tokens.size()-1).getParensDepth() != delimiter.getParensDepth()+1) {
            return false;
        }

        // if the previous token was not IF or REPEAT, it's not the function
        Token previousToken = getPreviousToken(tokens, delimiter.getParensDepth());
        if (previousToken == null || !("IF".equals(previousToken.getText()) || "REPEAT".equals(previousToken.getText()))) {
            return false;
        }

        return true;
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        String keywordText = keyword.getText();

        int parensDepth = keyword.getParensDepth();

        if (IfState.IF.equals(ifState)) {
            ifState = IfState.UNKNOWN;

            if ("EXISTS".equals(keywordText)) {
                ifState = IfState.IF_EXISTS;
            }

            if ("NOT".equals(keywordText)) {
                ifState = IfState.IF_NOT;
            }
        }

        if ("THEN".equals(keywordText)) {
            ifState = IfState.IF_THEN;
        }

        if ("IF".equals(keywordText) && !"END".equals(previousKeywordText) && !IfState.IF_FUNCTION.equals(ifState)) {
            if (IfState.IF_EXISTS.equals(ifState) || IfState.IF_NOT.equals(ifState)) {
                context.decreaseBlockDepth();
            }

            context.increaseBlockDepth(keywordText);
            if (reader.peekNextNonWhitespace() == '(') {
                ifState = IfState.IF_FUNCTION;
            } else {
                ifState = IfState.IF;
            }
        }

        if ("BEGIN".equals(keywordText) || (CONTROL_FLOW_KEYWORDS.contains(keywordText) && !lastTokenIs(tokens, parensDepth, "END"))) {
            context.increaseBlockDepth(keywordText);
        }

        if ("END".equals(keywordText)) {
            if (lastTokenIs(tokens, parensDepth, "ROW")) {
                // in mariadb ROW END is not a block closer. See https://mariadb.com/kb/en/temporal-data-tables/
            } else {
                context.decreaseBlockDepth();
                if (IfState.IF_THEN.equals(ifState)) {
                    ifState = IfState.NONE;
                }
            }
        }

        if ("AS".equals(keywordText) && IfState.IF_FUNCTION.equals(ifState)) {
            context.decreaseBlockDepth();
            ifState = IfState.NONE;
        }

        if (";".equals(keywordText) || TokenType.DELIMITER.equals(keyword.getType()) || TokenType.EOF.equals(keyword.getType())) {
            if (IfState.IF_NOT.equals(ifState) ||  IfState.IF_EXISTS.equals(ifState) || IfState.IF_FUNCTION.equals(ifState)) {
                context.decreaseBlockDepth();
                ifState = IfState.NONE;
            } else if (context.getBlockDepth() > 0 && doesDelimiterEndFunction(tokens, keyword)) {
                context.decreaseBlockDepth();
            }
        }

        previousKeywordText = keywordText;
    }
}