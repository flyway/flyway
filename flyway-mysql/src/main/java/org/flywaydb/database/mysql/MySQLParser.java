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
package org.flywaydb.database.mysql;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;

public class MySQLParser extends Parser {
    private static final char ALTERNATIVE_SINGLE_LINE_COMMENT = '#';

    private static final Pattern STORED_PROGRAM_REGEX = Pattern.compile(
            "^CREATE\\s(((DEFINER\\s@\\s)?(PROCEDURE|FUNCTION|EVENT))|TRIGGER)", Pattern.CASE_INSENSITIVE);
    private static final StatementType STORED_PROGRAM_STATEMENT = new StatementType();

    public MySQLParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 8);
    }

    @Override
    protected void resetDelimiter(ParserContext context) {
        // Do not reset delimiter as delimiter changes survive beyond a single statement
    }

    @Override
    protected Token handleKeyword(PeekingReader reader, ParserContext context, int pos, int line, int col, String keyword) throws IOException {
        if ("DELIMITER".equalsIgnoreCase(keyword)) {
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
                || (peek.charAt(0) == ALTERNATIVE_SINGLE_LINE_COMMENT && !isDelimiter(peek, context, col, 0)));
    }

    @Override
    protected Token handleStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow();
        reader.swallowUntilIncludingWithEscape('\'', true, '\\');
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow();
        reader.swallowUntilIncludingWithEscape('"', true, '\\');
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
    protected StatementType detectStatementType(String simplifiedStatement, ParserContext context, PeekingReader reader) {
        if (STORED_PROGRAM_REGEX.matcher(simplifiedStatement).matches()) {
            return STORED_PROGRAM_STATEMENT;
        }

        return super.detectStatementType(simplifiedStatement, context, reader);
    }

    @Override
    protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
        TokenType tokenType = token.getType();
        if (TokenType.DELIMITER.equals(tokenType) || ";".equals(token.getText())) {
            return true;
        } else if (TokenType.EOF.equals(tokenType)) {
            return true;
        }

        Token lastToken = getPreviousToken(tokens, context.getParensDepth());
        if (lastToken != null && lastToken.getType() == TokenType.KEYWORD) {
            return true;
        }

        return super.shouldAdjustBlockDepth(context, tokens, token);
    }

    private boolean doesDelimiterEndFunction(List<Token> tokens, Token delimiter) {
        // if there's not enough tokens, it's not the function
        if (tokens.size() < 2) {
            return false;
        }

        // if the previous keyword was not inside some brackets, it's not the function
        if (tokens.get(tokens.size() - 1).getParensDepth() != delimiter.getParensDepth() + 1) {
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
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
        String keywordText = keyword.getText();

        int parensDepth = keyword.getParensDepth();

        if ("BEGIN".equalsIgnoreCase(keywordText) && context.getStatementType() == STORED_PROGRAM_STATEMENT) {
            context.increaseBlockDepth(Integer.toString(parensDepth));
        }

        if (context.getBlockDepth() > 0 && lastTokenIs(tokens, parensDepth, "END") &&
                !"IF".equalsIgnoreCase(keywordText) && !"LOOP".equalsIgnoreCase(keywordText)) {
            String initiator = context.getBlockInitiator();
            if (initiator.equals("") || initiator.equals(keywordText) || "AS".equalsIgnoreCase(keywordText) || initiator.equals(Integer.toString(parensDepth))) {
                context.decreaseBlockDepth();
            }
        }

        if (";".equals(keywordText) || TokenType.DELIMITER.equals(keyword.getType()) || TokenType.EOF.equals(keyword.getType())) {
            if (context.getBlockDepth() > 0 && doesDelimiterEndFunction(tokens, keyword)) {
                context.decreaseBlockDepth();
            }
        }
    }
}