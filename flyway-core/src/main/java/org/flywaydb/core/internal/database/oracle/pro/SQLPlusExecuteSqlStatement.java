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

import java.sql.SQLException;
import java.util.List;

/**
 * A SQL*Plus EXECUTE statement.
 */
public class SQLPlusExecuteSqlStatement extends AbstractSqlStatement {
    public SQLPlusExecuteSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        return jdbcTemplate.executeStatement(errorContext, transformSql());
    }

    String transformSql() {
        String noExecute = sql.substring(sql.indexOf(" ") + 1);
        String noDash = noExecute.replaceAll("-\n", "\n");
        return "BEGIN\n" + noDash + ";\nEND;";
    }
}
