package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ErrorContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
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
    public void execute(ErrorContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        String option = sql.substring("WHENEVER SQLERROR ".length()).toUpperCase(Locale.ENGLISH);
        if ("CONTINUE".equals(option)) {
            errorContext.setSuppressErrors(true);
            return;
        }
        if ("EXIT FAILURE".equals(option)) {
            errorContext.setSuppressErrors(false);
            return;
        }
        LOG.warn("Unknown option for WHENEVER SQLERROR: " + option);
    }
}
