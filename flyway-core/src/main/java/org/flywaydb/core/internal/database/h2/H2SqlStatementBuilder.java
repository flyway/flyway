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
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting H2-specific delimiter changes.
 */
public class H2SqlStatementBuilder extends SqlStatementBuilder {
    public H2SqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("$$")) {
            return "$$";
        }
        return null;
    }
}
