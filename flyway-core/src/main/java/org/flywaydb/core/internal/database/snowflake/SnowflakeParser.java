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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;

public class SnowflakeParser extends Parser {
    private final String ALTERNATIVE_QUOTE = "$$";

    public SnowflakeParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    @Override
    protected boolean isAlternativeStringLiteral(String peek) {
        if (peek.startsWith("$$")) {
            return true;
        }

        return super.isAlternativeStringLiteral(peek);
    }

    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow(ALTERNATIVE_QUOTE.length());
        reader.swallowUntilExcluding(ALTERNATIVE_QUOTE);
        reader.swallow(ALTERNATIVE_QUOTE.length());
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    @Override
    protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
        return peek.startsWith("--") || peek.startsWith("//");
    }

}