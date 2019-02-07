/*
 * Copyright 2010-2019 Boxfuse GmbH
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
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

/**
 * A PostgreSQL COPY FROM STDIN statement.
 */
public class PostgreSQLCopyParsedStatement extends ParsedSqlStatement {
    /**
     * Delimiter of COPY statements.
     */
    private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true



    );

    private final String copyData;

    /**
     * Creates a new PostgreSQL COPY ... FROM STDIN statement.
     */
    public PostgreSQLCopyParsedStatement(int pos, int line, int col, String sql, String copyData) {
        super(pos, line, col, sql, COPY_DELIMITER, true



        );
        this.copyData = copyData;
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate



    ) {
        Results results = new Results();
        try {
            CopyManager copyManager = new CopyManager(jdbcTemplate.getConnection().unwrap(BaseConnection.class));
            try {
                long updateCount = copyManager.copyIn(getSql(), new StringReader(copyData));
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