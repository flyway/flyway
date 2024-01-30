package org.flywaydb.database.derby;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;

public class DerbyParser extends Parser {
    public DerbyParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected char getAlternativeStringLiteralQuote() {
        return '$';
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow(2);
        reader.swallowUntilExcluding("$$");
        reader.swallow(2);
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }
}