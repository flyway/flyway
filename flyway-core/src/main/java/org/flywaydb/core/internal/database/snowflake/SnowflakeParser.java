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

public class SnowflakeParser extends Parser {
    private static final String ALTERNATIVE_QUOTE = "$$";
    private static final String ALTERNATIVE_QUOTE_SCRIPT = "DECLARE";

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
            text = readBetweenRecursive(reader, alternativeQuoteOpen, alternativeQuoteEnd);
        } else {
            reader.swallow(alternativeQuoteOpen.length());
            text = reader.readUntilExcluding(alternativeQuoteOpen, alternativeQuoteEnd);
            reader.swallow(alternativeQuoteEnd.length());
        }

        return new Token(TokenType.STRING, pos, line, col, text, text, context.getParensDepth());
    }

    @Override
    protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
        return peek.startsWith("--") || peek.startsWith("//");
    }

    private String readBetweenRecursive(PeekingReader reader, String prefix, String suffix) throws IOException {
        StringBuilder result = new StringBuilder();
        reader.swallow(prefix.length());
        while (!reader.peek(suffix)) {
            result.append(reader.readUntilExcluding(prefix, suffix));
            if (reader.peek(prefix)) {
                result.append(prefix).append(readBetweenRecursive(reader, prefix, suffix)).append(suffix);
            }
        }
        reader.swallow(suffix.length());
        return result.toString();
    }
}