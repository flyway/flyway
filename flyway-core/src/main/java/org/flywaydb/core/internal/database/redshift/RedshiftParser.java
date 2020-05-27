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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class RedshiftParser extends Parser {
    private static final Pattern CREATE_LIBRARY_REGEX = Pattern.compile("^(CREATE|DROP) LIBRARY");
    private static final Pattern CREATE_EXTERNAL_TABLE_REGEX = Pattern.compile("^CREATE EXTERNAL TABLE");
    private static final Pattern VACUUM_REGEX = Pattern.compile("^VACUUM");
    private static final Pattern ALTER_TABLE_APPEND_FROM_REGEX = Pattern.compile("^ALTER TABLE( .*)? APPEND FROM");
    private static final Pattern ALTER_TABLE_ALTER_COLUMN_REGEX = Pattern.compile("^ALTER TABLE( .*)? ALTER COLUMN");

    public RedshiftParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected char getAlternativeStringLiteralQuote() {
        return '$';
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
        if (CREATE_LIBRARY_REGEX.matcher(simplifiedStatement).matches()
                || CREATE_EXTERNAL_TABLE_REGEX.matcher(simplifiedStatement).matches()
                || VACUUM_REGEX.matcher(simplifiedStatement).matches()
                || ALTER_TABLE_APPEND_FROM_REGEX.matcher(simplifiedStatement).matches()
                || ALTER_TABLE_ALTER_COLUMN_REGEX.matcher(simplifiedStatement).matches()) {
            return false;
        }

        return null;
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        String dollarQuote = (char) reader.read() + reader.readUntilIncluding('$');
        reader.swallowUntilExcluding(dollarQuote);
        reader.swallow(dollarQuote.length());
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }
}