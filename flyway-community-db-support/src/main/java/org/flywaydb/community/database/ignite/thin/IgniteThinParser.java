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
package org.flywaydb.community.database.ignite.thin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class IgniteThinParser extends Parser {
    private static final List<String> DDL_KEYWORDS = Arrays.asList("CREATE", "ALTER", "DROP");

    public IgniteThinParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    @Override
    protected char getAlternativeStringLiteralQuote() {
        return '$';
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        String dollarQuote = (char) reader.read() + reader.readUntilIncluding('$');
        reader.swallowUntilExcluding(dollarQuote);
        reader.swallow(dollarQuote.length());
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
        return keywords.stream().noneMatch(token -> token.getType().equals(TokenType.KEYWORD) && DDL_KEYWORDS.contains(token.getText()));
    }
}