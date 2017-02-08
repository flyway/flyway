package org.flywaydb.core.internal.dbsupport.hive;

import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class HiveSqlStatementBuilder extends SqlStatementBuilder {
    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("$$"))
            return "$$";
        else
            return null;
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("X"))
            return token.substring(token.indexOf("'"));
        else
            return super.cleanToken(token);
    }
}
