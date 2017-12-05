/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting Derby-specific delimiter changes.
 */
public class DerbySqlStatementBuilder extends SqlStatementBuilder {
    public DerbySqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("$$")) {
            return "$$";
        }
        return null;
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("X'")) {
            return token.substring(token.indexOf("'"));
        }
        return super.cleanToken(token);
    }
}
