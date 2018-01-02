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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting Sybase ASE-specific delimiter changes.
 */
public class SybaseASESqlStatementBuilder extends SqlStatementBuilder {
    SybaseASESqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

	@Override
    protected String computeAlternateCloseQuote(String openQuote) {
        char specialChar = openQuote.charAt(2);
        switch (specialChar) {
            case '(':
                return ")'";
            default:
                return specialChar + "'";
        }
    }
}
