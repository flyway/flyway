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
package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * A SQL*Plus REMARK statement.
 */
public class SQLPlusRemarkSqlStatement extends AbstractSqlStatement {
    public SQLPlusRemarkSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) {
        // Do nothing as this SQL*Plus comment can safely be ignored
        return new ArrayList<Result>();
    }
}
