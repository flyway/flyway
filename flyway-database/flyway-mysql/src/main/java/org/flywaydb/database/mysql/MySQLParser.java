/*-
 * ========================LICENSE_START=================================
 * flyway-mysql
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
            "^CREATE\\s(((DEFINER\\s(\\w+\\s)?@\\s(\\w+\\s)?)?(PROCEDURE|FUNCTION|EVENT))|TRIGGER)", Pattern.CASE_INSENSITIVE);
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
            String text = "";
            while (text.isEmpty()) {
                text = reader.readUntilExcluding('\n', '\r').trim();
                reader.swallow(1);
            }
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
        // we assume that any blocks opened or closed inside some parens
        // can't affect block depth outside those parens
        return token.getParensDepth() == 0;
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
        String keywordText = keyword.getText();

        int parensDepth = keyword.getParensDepth();

        if ("BEGIN".equalsIgnoreCase(keywordText) && context.getStatementType() == STORED_PROGRAM_STATEMENT) {
            // BEGIN ... END is the usual way to define a nested block
            context.increaseBlockDepth("");
        }

        if ("CASE".equalsIgnoreCase(keywordText)) {
            // CASE is treated specially compared to IF or LOOP since it can either be
            // a statement or an expression. CASE statements are terminated with END CASE,
            // while CASE expressions are only terminated with END.
            //
            // We need to decide if some END token ends a CASE statement or a block. Since
            // we can't easily tell if we're in a CASE statement or expression, we don't
            // know if we should be expecting END or END CASE. So we'll just assume that
            // END always closes a CASE, and then prevent END CASE from starting
            // a new one:
            if (!lastTokenIs(tokens, parensDepth, "END")) {
                context.increaseBlockDepth("");
            }

            // we could do something similar for the other control flow keywords, but IF and
            // REPEAT in particular would be tricky since they are also the names of functions
            // (and functions don't have a matching END keyword).
        }

        // END always ends a block, unless it's part of END IF or END LOOP etc
        //
        // this is a little tricky since we can't peek ahead at tokens, so we have to
        // wait until one token *after* the END and decrease the block depth there.
        if (context.getBlockDepth() > 0
            && lastTokenIs(tokens, parensDepth, "END")
            && !"IF".equalsIgnoreCase(keywordText)
            && !"LOOP".equalsIgnoreCase(keywordText)
            && !"REPEAT".equalsIgnoreCase(keywordText)
            && !"WHILE".equalsIgnoreCase(keywordText)) {
            context.decreaseBlockDepth();
        }
    }
}
