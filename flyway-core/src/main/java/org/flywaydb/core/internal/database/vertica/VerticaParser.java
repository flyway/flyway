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
package org.flywaydb.core.internal.database.vertica;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Vertica Parser
 */
public class VerticaParser extends Parser {
    private static final Pattern COPY_FROM_STDIN_REGEX = Pattern.compile("^COPY( .*)? FROM STDIN");
    private static final StatementType COPY_FROM_STDIN = new StatementType();

    public VerticaParser(Configuration configuration, ParsingContext parsingContext, int peekDepth) {
        super(configuration, parsingContext, peekDepth);
    }

    public VerticaParser(Configuration configuration, ParsingContext parsingContext) {
        this(configuration, parsingContext, 1);
    }

    @Override
    protected char getAlternativeStringLiteralQuote() {
        return '$';
    }

    @Override
    protected ParsedSqlStatement createStatement(PeekingReader reader, Recorder recorder,
                                                 int statementPos, int statementLine, int statementCol,
                                                 int nonCommentPartPos, int nonCommentPartLine, int nonCommentPartCol,
                                                 StatementType statementType, boolean canExecuteInTransaction,
                                                 Delimiter delimiter, String sql


    ) throws IOException {
        if (statementType == COPY_FROM_STDIN) {
            throw new UnsupportedOperationException("COPY FROM STDIN is not supported currently");
        }
        return super.createStatement(reader, recorder, statementPos, statementLine, statementCol,
                nonCommentPartPos, nonCommentPartLine, nonCommentPartCol,
                statementType, canExecuteInTransaction, delimiter, sql
        );
    }

    @Override
    protected StatementType detectStatementType(String simplifiedStatement) {
        if (COPY_FROM_STDIN_REGEX.matcher(simplifiedStatement).matches()) {
            return COPY_FROM_STDIN;
        }

        return super.detectStatementType(simplifiedStatement);
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
