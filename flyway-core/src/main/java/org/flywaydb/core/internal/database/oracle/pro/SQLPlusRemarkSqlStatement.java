package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ErrorContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * A SQL*Plus REMARK statement.
 */
public class SQLPlusRemarkSqlStatement extends AbstractSqlStatement {
    public SQLPlusRemarkSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public void execute(ErrorContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        // Do nothing as this SQL*Plus comment can safely be ignored
    }
}
