package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ErrorContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
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
    public void execute(ErrorContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        String option = sql.substring("SET ".length()).toUpperCase(Locale.ENGLISH);
        if (option.matches("SERVEROUT(PUT) ON( SIZE ([0-9]{4,7}|UNL(IMITED)?))?")) {
            String size = option.contains("SIZE") ? option.substring(option.lastIndexOf(" " + 1)) : "UNLIMITED";
            jdbcTemplate.execute("BEGIN\nDBMS_OUTPUT.ENABLE"
                    + (size.matches("[0-9]{4,7}") ? "(" + size + ")" : "")
                    + ";\nEND;");
            errorContext.setServerOutput(true);
            return;
        }
        if (option.matches("SERVEROUT(PUT)? OFF")) {
            jdbcTemplate.execute("BEGIN\nDBMS_OUTPUT.DISABLE;\nEND;");
            errorContext.setServerOutput(false);
            return;
        }
        LOG.warn("Unknown option for SET: " + option);
    }
}
