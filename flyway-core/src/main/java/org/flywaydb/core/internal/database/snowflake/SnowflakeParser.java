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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SnowflakeParser extends Parser {
    private static final String ALTERNATIVE_QUOTE = "$$";
    private static final String ALTERNATIVE_QUOTE_SCRIPT = "DECLARE";
    private static final List<String> CONDITIONALLY_CREATABLE_OBJECTS = Arrays.asList(
        "COLUMN", "CONNECTION", "CONSTRAINT", "DATABASE", "FORMAT", "FUNCTION", "GROUP", "INDEX", "INTEGRATION", "PIPE", "POLICY", "PROCEDURE", "ROLE",
        "SCHEMA", "SEQUENCE", "STAGE", "STREAM", "TABLE", "TAG", "TASK", "USER", "VIEW", "WAREHOUSE"
     );

    public SnowflakeParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 7);
    }

    @Override
    protected boolean isAlternativeStringLiteral(String peek) {
        if (peek.startsWith(ALTERNATIVE_QUOTE) || peek.toUpperCase().startsWith(ALTERNATIVE_QUOTE_SCRIPT)) {
            return true;
        }
        return super.isAlternativeStringLiteral(peek);
    }

    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        String alternativeQuoteOpen = ALTERNATIVE_QUOTE;
        String alternativeQuoteEnd = ALTERNATIVE_QUOTE;

        String text;
        if (reader.peek(ALTERNATIVE_QUOTE_SCRIPT)) {
            alternativeQuoteOpen = "BEGIN";
            alternativeQuoteEnd = "END";
            reader.swallowUntilExcluding(alternativeQuoteOpen);
            text = readBetweenRecursive(reader, alternativeQuoteOpen, alternativeQuoteEnd, context.getDelimiter().toString().charAt(0));
        } else {
            reader.swallow(alternativeQuoteOpen.length());
            text = reader.readUntilExcluding(alternativeQuoteOpen, alternativeQuoteEnd);
            reader.swallow(alternativeQuoteEnd.length());
        }

        return new Token(TokenType.STRING, pos, line, col, text, text, context.getParensDepth());
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        int lastKeywordIndex = getLastKeywordIndex(tokens);
        Token previousKeyword = lastKeywordIndex >= 0 ? tokens.get(lastKeywordIndex) : null;
        String keywordText = keyword.getText();
        String previousKeywordText = previousKeyword != null ? previousKeyword.getText().toUpperCase(Locale.ENGLISH) : "";

        if ("BEGIN".equalsIgnoreCase(keywordText) &&
                (reader.peekIgnoreCase(" TRANSACTION") || reader.peekIgnoreCase(context.getDelimiter().toString()) || reader.peekIgnoreCase(" WORK") || reader.peekIgnoreCase(" NAME"))) {
            return; //Beginning a transaction shouldn't increase block depth
        }

        if ("BEGIN".equalsIgnoreCase(keywordText)
                || ((("IF".equalsIgnoreCase(keywordText) && !CONDITIONALLY_CREATABLE_OBJECTS.contains(previousKeywordText))  // excludes the IF in eg. CREATE TABLE IF EXISTS
                || "FOR".equalsIgnoreCase(keywordText)
                || "CASE".equalsIgnoreCase(keywordText))
                && previousKeyword != null && !"END".equalsIgnoreCase(previousKeywordText)
                && !"CURSOR".equalsIgnoreCase(previousKeywordText))) {  // DECLARE CURSOR FOR SELECT ... has no END
            context.increaseBlockDepth(keywordText);
        } else if (("EACH".equalsIgnoreCase(keywordText) || "SQLEXCEPTION".equalsIgnoreCase(keywordText))
                && previousKeyword != null && "FOR".equalsIgnoreCase(previousKeywordText) && context.getBlockDepth() > 0) {
            context.decreaseBlockDepth();
        } else if ("END".equalsIgnoreCase(keywordText) && context.getBlockDepth() > 0) {
            context.decreaseBlockDepth();
        }
    }

    @Override
    protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
        return peek.startsWith("--") || peek.startsWith("//");
    }

    private String readBetweenRecursive(PeekingReader reader, String prefix, String suffix, char delimiter) throws IOException {
        StringBuilder result = new StringBuilder();
        reader.swallow(prefix.length());
        while (!reader.peek(suffix)) {
            result.append(reader.readUntilExcluding(prefix, suffix));
            if (reader.peekIgnoreCase("END IF") || reader.peekIgnoreCase("END FOR") || reader.peekIgnoreCase("END CASE")) {
                result.append(reader.readUntilIncluding(delimiter));
                result.append(reader.readUntilExcluding(prefix, suffix));
            }
            if (reader.peek(prefix)) {
                result.append(prefix).append(readBetweenRecursive(reader, prefix, suffix, delimiter)).append(suffix);
            }
        }
        reader.swallow(suffix.length());
        return result.toString();
    }
}