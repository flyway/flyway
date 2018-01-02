/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * SqlStatementBuilder supporting H2-specific delimiter changes.
 */
public class SQLiteSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    SQLiteSqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (StringUtils.countOccurrencesOf(statementStart, " ") < 8) {
            statementStart += line;
            statementStart += " ";
            statementStart = statementStart.replaceAll("\\s+", " ");
        }
        boolean createTriggerStatement = statementStart.matches("CREATE( TEMP| TEMPORARY)? TRIGGER.*");

        if (createTriggerStatement && !line.endsWith("END;")) {
            return null;
        }
        return defaultDelimiter;
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("X'")) {
            // blob literal
            return token.substring(token.indexOf("'"));
        }
        return token;
    }

}
