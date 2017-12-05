/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * A SQL*Plus PROMPT statement.
 */
public class SQLPlusPromptSqlStatement extends AbstractSqlStatement {
    private static final Log LOG = LogFactory.getLog(SQLPlusPromptSqlStatement.class);

    public SQLPlusPromptSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) {
        LOG.info(transformSql());
        return new ArrayList<Result>();
    }

    String transformSql() {
        String noPrompt = sql.substring(sql.indexOf(" ") + 1);
        return noPrompt.replaceAll("-\n", "\n");
    }
}
