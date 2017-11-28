package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.database.JdbcTemplate;

import java.sql.SQLException;

/**
 * A SQL*Plus PROMPT statement.
 */
public class SQLPlusPromptSqlStatement extends AbstractSqlStatement {
    private static final Log LOG = LogFactory.getLog(SQLPlusPromptSqlStatement.class);

    public SQLPlusPromptSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public void execute(JdbcTemplate jdbcTemplate) throws SQLException {
        LOG.info(transformSql());
    }

    String transformSql() {
        String noPrompt = sql.substring(sql.indexOf(" ") + 1);
        return noPrompt.replaceAll("-\n", "\n");
    }
}
