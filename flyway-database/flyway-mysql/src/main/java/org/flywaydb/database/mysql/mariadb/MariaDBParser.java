package org.flywaydb.database.mysql.mariadb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.database.mysql.MySQLParser;
import org.flywaydb.core.internal.parser.*;

import java.util.List;
import java.util.regex.Pattern;

public class MariaDBParser extends MySQLParser {
    private static final Pattern BEGIN_NOT_ATOMIC_REGEX = Pattern.compile(
            "^BEGIN\\sNOT\\sATOMIC\\s.*END", Pattern.CASE_INSENSITIVE);
    private static final StatementType BEGIN_NOT_ATOMIC_STATEMENT = new StatementType();

    public MariaDBParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext);
    }

    @Override
    protected StatementType detectStatementType(String simplifiedStatement, ParserContext context, PeekingReader reader) {
        if (BEGIN_NOT_ATOMIC_REGEX.matcher(simplifiedStatement).matches()) {
            return BEGIN_NOT_ATOMIC_STATEMENT;
        }

        return super.detectStatementType(simplifiedStatement, context, reader);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
        String keywordText = keyword.getText();

        if (lastTokenIs(tokens, context.getParensDepth(), "NOT") && "ATOMIC".equalsIgnoreCase(keywordText)) {
            context.increaseBlockDepth("");
        }

        if (context.getBlockDepth() > 0 && context.getStatementType() == BEGIN_NOT_ATOMIC_STATEMENT &&
                keywordText.equalsIgnoreCase("END")) {
            context.decreaseBlockDepth();
        }

        super.adjustBlockDepth(context, tokens, keyword, reader);
    }
}