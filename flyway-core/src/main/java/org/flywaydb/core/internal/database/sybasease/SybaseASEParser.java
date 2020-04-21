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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.io.IOException;

public class SybaseASEParser extends Parser {
    public SybaseASEParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected boolean isDelimiter(String peek, ParserContext context, int col) {
        return peek.length() >= 2
                && (peek.charAt(0) == 'G' || peek.charAt(0) == 'g')
                && (peek.charAt(1) == 'O' || peek.charAt(1) == 'o')
                && (peek.length() == 2 || Character.isWhitespace(peek.charAt(2)));
    }

    @Override
    protected String readKeyword(PeekingReader reader, Delimiter delimiter, ParserContext context) throws IOException {
        // #2414: Ignore delimiter as GO (unlike ;) can be part of a regular keyword
        return "" + (char) reader.read() + reader.readKeywordPart(null, context);
    }
}