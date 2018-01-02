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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting HSQLDB-specific delimiter changes.
 */
public class HSQLDBSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Are we inside a BEGIN ATOMIC block.
     */
    private boolean insideAtomicBlock;

    HSQLDBSqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.contains("BEGIN ATOMIC")) {
            insideAtomicBlock = true;
        }

        if (line.endsWith("END;")) {
            insideAtomicBlock = false;
        }

        if (insideAtomicBlock) {
            return null;
        }
        return defaultDelimiter;
    }
}
