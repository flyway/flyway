package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ErrorContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * A SQL*Plus EXECUTE statement.
 */
public class SQLPlusExecuteSqlStatement extends AbstractSqlStatement {
    public SQLPlusExecuteSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public void execute(ErrorContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        jdbcTemplate.executeStatement(errorContext, transformSql());
    }

    String transformSql() {
        String noExecute = sql.substring(sql.indexOf(" ") + 1);
        String noDash = noExecute.replaceAll("-\n", "\n");
        return "BEGIN\n" + noDash + ";\nEND;";
    }
}
