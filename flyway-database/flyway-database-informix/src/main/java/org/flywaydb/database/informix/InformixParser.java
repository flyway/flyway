package org.flywaydb.database.informix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.List;

public class InformixParser extends Parser {
    public InformixParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        int lastKeywordIndex = getLastKeywordIndex(tokens);
        if (lastKeywordIndex < 0) {
            return;
        }

        String current = keyword.getText();
        if ("FUNCTION".equals(current) || "PROCEDURE".equals(current)) {
            String previous = tokens.get(lastKeywordIndex).getText();

            // CREATE( DBA)? (FUNCTION|PROCEDURE)
            if ("CREATE".equals(previous) || "DBA".equals(previous)) {
                context.increaseBlockDepth(previous);
            } else if ("END".equals(previous)) {
                context.decreaseBlockDepth();
            }
        }
    }
}