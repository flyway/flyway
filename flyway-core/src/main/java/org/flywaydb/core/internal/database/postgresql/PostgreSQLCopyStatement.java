/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
