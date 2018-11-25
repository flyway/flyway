/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.sqlscript.AbstractSqlStatement;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;

/**
 * A PostgreSQL COPY FROM STDIN statement.
 */
public class PostgreSQLCopyStatement extends AbstractSqlStatement {
    /**
     * Delimiter of COPY statements.
     */
    static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true);

    /**
     * Creates a new sql statement.
     *
     * @param lines The lines of the statement.
     */
    PostgreSQLCopyStatement(List<Line> lines) {
        super(lines, COPY_DELIMITER



        );
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate, SqlScriptExecutor sqlScriptExecutor) {
        String sql = getSql();
        int split = sql.indexOf(";");

        String statement = sql.substring(0, split);

        String data = sql.substring(split + 1);
        // Strip optional linebreak
        StringBuilder buf = new StringBuilder(data);
        while (buf.length() > 0 && ((buf.charAt(0) == '\r') || (buf.charAt(0) == '\n'))) {
            buf.deleteCharAt(0);
        }
        data = buf.toString();

        Results results = new Results();
        try {
            CopyManager copyManager = new CopyManager(jdbcTemplate.getConnection().unwrap(BaseConnection.class));
            try {
                long updateCount = copyManager.copyIn(statement, new StringReader(data));
                results.addResult(new Result(updateCount



                ));
            } catch (IOException e) {
                throw new SQLException("Unable to execute COPY operation", e);
            }
        } catch (SQLException e) {
            jdbcTemplate.extractErrors(results, e);
        }
        return results;
    }
}