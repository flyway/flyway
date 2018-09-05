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
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.jdbc.ErrorImpl;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.StandardContext;
import org.flywaydb.core.internal.util.AsciiTable;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSqlScriptExecutor implements SqlScriptExecutor {
    private static final Log LOG = LogFactory.getLog(DefaultSqlScriptExecutor.class);

    private final JdbcTemplate jdbcTemplate;




























    public DefaultSqlScriptExecutor(JdbcTemplate jdbcTemplate




    ) {
        this.jdbcTemplate = jdbcTemplate;






    }

    @Override
    public void execute(SqlScript sqlScript) {








        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        for (int i = 0; i < sqlStatements.size(); i++) {
            SqlStatement sqlStatement = sqlStatements.get(i);
            String sql = sqlStatement.getSql();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing "



                        + "SQL: " + sql);
            }























                executeStatement(jdbcTemplate, sqlScript, sqlStatement);



        }
    }

















































    private void executeStatement(JdbcTemplate jdbcTemplate, SqlScript sqlScript, SqlStatement sqlStatement) {
        StandardContext context = createContext();

        String sql = sqlStatement.getSql() + sqlStatement.getDelimiter();
        try {






            List<Result> results = sqlStatement.execute(context, jdbcTemplate, this);







            printWarnings(context);
            handleResults(context, results);
        } catch (final SQLException e) {










            printWarnings(context);





            handleException(e, sqlScript, sqlStatement, context);
        }
    }

    private void handleResults(StandardContext context, List<Result> results) {
        for (Result result : results) {
            long updateCount = result.getUpdateCount();
            if (updateCount != -1) {
                handleUpdateCount(updateCount);
            }





        }
    }








    private void handleUpdateCount(long updateCount) {
        LOG.debug("Update Count: " + updateCount);
    }

    protected void handleException(SQLException e, SqlScript sqlScript, SqlStatement sqlStatement, StandardContext context) {
        throw new FlywaySqlScriptException(sqlScript.getResource(), sqlStatement, e);
    }

    protected StandardContext createContext() {
        return new StandardContext();
    }






    private void printWarnings(StandardContext context) {
        for (Warning warning : context.getWarnings()) {
            if ("00000".equals(warning.getState())) {
                LOG.info("DB: " + warning.getMessage());
            } else {
                LOG.warn("DB: " + warning.getMessage()
                        + " (SQL State: " + warning.getState() + " - Error Code: " + warning.getCode() + ")");
            }
        }
    }
}