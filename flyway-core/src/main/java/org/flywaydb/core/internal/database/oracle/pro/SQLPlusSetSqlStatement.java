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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A SQL*Plus SET statement.
 */
public class SQLPlusSetSqlStatement extends AbstractSqlStatement {
    private static final Log LOG = LogFactory.getLog(SQLPlusSetSqlStatement.class);

    public SQLPlusSetSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl context, JdbcTemplate jdbcTemplate) throws SQLException {
        String option = sql.substring("SET ".length()).toUpperCase(Locale.ENGLISH);
        if (option.matches("SERVEROUT(PUT) ON( SIZE ([0-9]{4,7}|UNL(IMITED)?))?")) {
            String size = option.contains("SIZE") ? option.substring(option.lastIndexOf(" " + 1)) : "UNLIMITED";
            jdbcTemplate.execute("BEGIN\nDBMS_OUTPUT.ENABLE"
                    + (size.matches("[0-9]{4,7}") ? "(" + size + ")" : "")
                    + ";\nEND;");
            context.setServerOutput(true);
        } else if (option.matches("SERVEROUT(PUT)? OFF")) {
            jdbcTemplate.execute("BEGIN\nDBMS_OUTPUT.DISABLE;\nEND;");
            context.setServerOutput(false);
        } else if (option.matches("NULL .+")) {
            String nullText = getNullText(option);
            context.setNullText(nullText);
        } else {
            LOG.warn("Unknown option for SET: " + option);
        }
        return new ArrayList<Result>();
    }

    static String getNullText(String option) {
        String nullText = option.substring(5);
        if (nullText.startsWith("\"")) {
            nullText = nullText.substring(1);
        }
        if (nullText.endsWith("\"")) {
            nullText = nullText.substring(0, nullText.length() - 1);
        }
        return nullText;
    }
}
