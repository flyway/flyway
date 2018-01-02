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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A PostgreSQL COPY FROM STDIN statement.
 */
public class PostgreSQLCopyStatement extends AbstractSqlStatement {
    /**
     * Creates a new sql statement.
     *
     * @param lineNumber The original line number where the statement was located in the script it came from.
     * @param sql        The sql to send to the database.
     */
    PostgreSQLCopyStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        int split = sql.indexOf(";");
        String statement = sql.substring(0, split);
        String data = sql.substring(split + 1).trim();

        List<Result> results = new ArrayList<Result>();
        CopyManager copyManager = new CopyManager(jdbcTemplate.getConnection().unwrap(BaseConnection.class));
        try {
            long updateCount = copyManager.copyIn(statement, new StringReader(data));
            results.add(new Result(updateCount
                    // [pro]
                    , null, null
                    // [/pro]
            ));
        } catch (IOException e) {
            throw new SQLException("Unable to execute COPY operation", e);
        }
        return results;
    }
}
