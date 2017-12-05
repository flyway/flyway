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
package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A SQL*Plus WHENEVER SQLERROR statement.
 */
public class SQLPlusWheneverSqlerrorSqlStatement extends AbstractSqlStatement {
    private static final Log LOG = LogFactory.getLog(SQLPlusWheneverSqlerrorSqlStatement.class);

    public SQLPlusWheneverSqlerrorSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) {
        String option = sql.substring("WHENEVER SQLERROR ".length()).toUpperCase(Locale.ENGLISH);
        if ("CONTINUE".equals(option)) {
            errorContext.setSuppressErrors(true);
        } else if ("EXIT FAILURE".equals(option)) {
            errorContext.setSuppressErrors(false);
        } else {
            LOG.warn("Unknown option for WHENEVER SQLERROR: " + option);
        }
        return new ArrayList<Result>();
    }
}
