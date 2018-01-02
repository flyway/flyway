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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.sqlscript.SqlStatement;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public abstract class AbstractSqlStatement implements SqlStatement {
    /**
     * The original line number where the statement was located in the script it came from.
     */
    protected int lineNumber;

    /**
     * The sql to send to the database.
     */
    protected String sql;

    public AbstractSqlStatement(int lineNumber, String sql) {
        this.lineNumber = lineNumber;
        this.sql = sql;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
