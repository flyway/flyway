/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

import java.io.IOException;
import java.util.List;

public class DB2Parser extends Parser {
    private static final String COMMENT_DIRECTIVE = "--#";
    private static final String SET_TERMINATOR_DIRECTIVE = COMMENT_DIRECTIVE + "SET TERMINATOR ";

    public DB2Parser(Configuration configuration) {
        super(configuration, COMMENT_DIRECTIVE.length());
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> keywords) {
        if (keywords.size() < 2) {
            return;
        }
        Token token = keywords.get(keywords.size() - 1);
        Token previousToken = keywords.get(keywords.size() - 2);
        Token previousPreviousToken = keywords.size() > 2 ? keywords.get(keywords.size() - 3) : null;
        if (
            // BEGIN increases block depth, exception when used with ROW BEGIN
                ("BEGIN".equals(token.getText())
                        && (!"ROW".equals(previousToken.getText())
                        || previousPreviousToken == null || "EACH".equals(previousPreviousToken.getText())))
                        // CASE, DO, IF and REPEAT increase block depth
                        || (("CASE".equals(token.getText()) || "DO".equals(token.getText())
                        || "IF".equals(token.getText()) || "REPEAT".equals(token.getText()))
                        // But not END IF and END WHILE
                        && !"END".equals(previousToken.getText()))) {
            context.increaseBlockDepth();
        } else if (
            // END decreases block depth, exception when used with ROW END
                "END".equals(token.getText()) && !"ROW".equals(previousToken.getText())) {
            context.decreaseBlockDepth();
        }
    }

    @Override
    protected boolean isCommentDirective(String peek) {
        return peek.startsWith(COMMENT_DIRECTIVE);
    }

    @Override
    protected Token handleCommentDirective(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        if (SET_TERMINATOR_DIRECTIVE.equals(reader.peek(SET_TERMINATOR_DIRECTIVE.length()))) {
            reader.swallow(SET_TERMINATOR_DIRECTIVE.length());
            String delimiter = reader.readUntilExcluding('\n', '\r');
            return new Token(TokenType.NEW_DELIMITER, pos, line, col, delimiter.trim(), context.getParensDepth());
        }
        reader.swallowUntilExcluding('\n', '\r');
        return new Token(TokenType.COMMENT, pos, line, col, null, context.getParensDepth());
    }
}