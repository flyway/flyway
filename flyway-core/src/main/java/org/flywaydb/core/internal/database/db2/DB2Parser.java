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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DB2Parser extends Parser {
    private static final String COMMENT_DIRECTIVE = "--#";
    private static final String SET_TERMINATOR_DIRECTIVE = COMMENT_DIRECTIVE + "SET TERMINATOR ";

    public DB2Parser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, COMMENT_DIRECTIVE.length());
    }

    // WHILE and FOR both contain DO before the body of the block, so are both handled by the DO keyword
    // See https://www.ibm.com/support/knowledgecenter/en/SSEPEK_10.0.0/sqlref/src/tpc/db2z_sqlplnativeintro.html
    private static final List<String> CONTROL_FLOW_KEYWORDS = Arrays.asList("LOOP", "CASE", "DO", "REPEAT", "IF");

    private static final Pattern CREATE_IF_NOT_EXISTS = Pattern.compile(
            ".*CREATE\\s([^\\s]+\\s){0,2}IF\\sNOT\\sEXISTS");
    private static final Pattern DROP_IF_EXISTS = Pattern.compile(
            ".*DROP\\s([^\\s]+\\s){0,2}IF\\sEXISTS");

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        boolean previousTokenIsKeyword = !tokens.isEmpty() && tokens.get(tokens.size() - 1).getType() == TokenType.KEYWORD;

        int lastKeywordIndex = getLastKeywordIndex(tokens);
        String previousKeyword = lastKeywordIndex >= 0 ? tokens.get(lastKeywordIndex).getText() : null;

        lastKeywordIndex = getLastKeywordIndex(tokens, lastKeywordIndex);
        String previousPreviousToken = lastKeywordIndex >= 0 ? tokens.get(lastKeywordIndex).getText() : null;

        if (
            // BEGIN increases block depth, exception when used with ROW BEGIN
                ("BEGIN".equals(keyword.getText()) && (!"ROW".equals(previousKeyword) || previousPreviousToken == null || "EACH".equals(previousPreviousToken)))
                        // Control flow keywords increase depth
                        || CONTROL_FLOW_KEYWORDS.contains(keyword.getText())
                       ) {
            // But not END IF and END WHILE
            if (!previousTokenIsKeyword || !"END".equals(previousKeyword)) {
                context.increaseBlockDepth(keyword.getText());

            }
        } else if (
            // END decreases block depth, exception when used with ROW END
                ("END".equals(keyword.getText()) && !"ROW".equals(previousKeyword))
                        || doTokensMatchPattern(tokens, keyword, CREATE_IF_NOT_EXISTS)
                        || doTokensMatchPattern(tokens, keyword, DROP_IF_EXISTS)) {
            context.decreaseBlockDepth();
        }
    }

    @Override
    protected void resetDelimiter(ParserContext context) {
        // Do not reset delimiter as delimiter changes survive beyond a single statement
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
            return new Token(TokenType.NEW_DELIMITER, pos, line, col, delimiter.trim(), delimiter, context.getParensDepth());
        }
        reader.swallowUntilExcluding('\n', '\r');
        return new Token(TokenType.COMMENT, pos, line, col, null, null, context.getParensDepth());
    }
}