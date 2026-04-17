/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.sqlscript;

import lombok.CustomLog;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.exception.FlywayBlockStatementExecutionException;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CustomLog
public class DefaultSqlScriptExecutor implements SqlScriptExecutor {
    protected final JdbcTemplate jdbcTemplate;

    /**
     * The callback executor.
     */
    private final CallbackExecutor<Event> callbackExecutor;













    /**
     * The maximum number of statements to include in a batch.
     */
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * Whether to batch SQL statements.
     */
    private final boolean batch;

    /**
     * Whether to output query results table.
     */
    protected final boolean outputQueryResults;

    public DefaultSqlScriptExecutor(JdbcTemplate jdbcTemplate,
                                    CallbackExecutor<Event> callbackExecutor, boolean undo, boolean batch, boolean outputQueryResults,
                                    StatementInterceptor statementInterceptor
                                   ) {
        this.jdbcTemplate = jdbcTemplate;




        this.callbackExecutor = callbackExecutor;
        this.outputQueryResults = outputQueryResults;
        this.batch = batch;
    }

    @Override
    public List<Results> execute(SqlScript sqlScript, Configuration config) {
        final List<Results> results = new ArrayList<>(List.of());






        List<SqlStatement> batchStatements = new ArrayList<>();

        try (SqlStatementIterator sqlStatementIterator = sqlScript.getSqlStatements()) {
            SqlStatement sqlStatement;
            while ((sqlStatement = sqlStatementIterator.next()) != null) {













                if (batch) {
                    if (sqlStatement.isBatchable()) {
                        logStatementExecution(sqlStatement);
                        batchStatements.add(sqlStatement);
                        if (batchStatements.size() >= MAX_BATCH_SIZE) {
                            results.add(executeBatch(jdbcTemplate, sqlScript, batchStatements, config));
                            batchStatements = new ArrayList<>();
                        }
                    } else {
                        // Execute the batch up to this point
                        results.add(executeBatch(jdbcTemplate, sqlScript, batchStatements, config));
                        batchStatements = new ArrayList<>();
                        // Now execute this non-batchable statement. We'll resume batching after this one.
                        results.add(executeStatement(jdbcTemplate, sqlScript, sqlStatement, config));
                    }
                } else {
                    results.add(executeStatement(jdbcTemplate, sqlScript, sqlStatement, config));
                }
            }
        }

        if (batch) {
            // Execute any remaining batch statements that haven't yet been sent to the database
            results.add(executeBatch(jdbcTemplate, sqlScript, batchStatements, config));
        }
        return results;
    }

    protected void logStatementExecution(SqlStatement sqlStatement) {
        LOG.debug("Executing "
            + (batch && sqlStatement.isBatchable() ? "batchable " : "")
            + "SQL: " + sqlStatement.getSql());
    }

    private Results executeBatch(JdbcTemplate jdbcTemplate, SqlScript sqlScript, List<SqlStatement> batchStatements, Configuration config) {
        if (batchStatements.isEmpty()) {
            return null;
        }

        LOG.debug("Sending batch of " + batchStatements.size() + " statements to database ...");
        List<String> sqlBatch = new ArrayList<>();
        for (SqlStatement sqlStatement : batchStatements) {
            try {
                handleEachMigrateOrUndoStatementCallback(Event.BEFORE_EACH_UNDO_STATEMENT, Event.BEFORE_EACH_MIGRATE_STATEMENT, sqlStatement.getSql() + sqlStatement.getDelimiter(), null, null);
            } catch (FlywayBlockStatementExecutionException e) {
                LOG.debug("Statement on line " + sqlStatement.getLineNumber() + " + skipped due to " + e.getMessage());
                continue;
            }
            sqlBatch.add(sqlStatement.getSql());
        }

        Results results = jdbcTemplate.executeBatch(sqlBatch);

        if (results.getException() != null) {

            for (int i = 0; i < results.getResults().size(); i++) {
                SqlStatement sqlStatement = batchStatements.get(i);
                long updateCount = results.getResults().get(i).updateCount();
                if (updateCount == Statement.EXECUTE_FAILED) {
                    handleEachMigrateOrUndoStatementCallback(Event.AFTER_EACH_UNDO_STATEMENT_ERROR, Event.AFTER_EACH_MIGRATE_STATEMENT_ERROR, sqlStatement.getSql() + sqlStatement.getDelimiter(), results.getWarnings(), results.getErrors());
                    handleException(results, sqlScript, batchStatements.get(i), config);
                } else if (updateCount != Statement.SUCCESS_NO_INFO) {
                    handleEachMigrateOrUndoStatementCallback(Event.AFTER_EACH_UNDO_STATEMENT, Event.AFTER_EACH_MIGRATE_STATEMENT, sqlStatement.getSql() + sqlStatement.getDelimiter(), results.getWarnings(), results.getErrors());
                    handleUpdateCount(updateCount);
                }
            }
            handleException(results, sqlScript, batchStatements.get(0), config);
            return results;
        }

        for (int i = 0; i < results.getResults().size(); i++) {
            SqlStatement sqlStatement = batchStatements.get(i);
            handleEachMigrateOrUndoStatementCallback(Event.AFTER_EACH_UNDO_STATEMENT, Event.AFTER_EACH_MIGRATE_STATEMENT, sqlStatement.getSql() + sqlStatement.getDelimiter(), results.getWarnings(), results.getErrors());
        }
        handleResults(results);
        return results;
    }

    protected Results executeStatement(JdbcTemplate jdbcTemplate, SqlScript sqlScript, SqlStatement sqlStatement, Configuration config) {
        logStatementExecution(sqlStatement);
        String sql = sqlStatement.getSql() + sqlStatement.getDelimiter();

        try {
            handleEachMigrateOrUndoStatementCallback(Event.BEFORE_EACH_UNDO_STATEMENT, Event.BEFORE_EACH_MIGRATE_STATEMENT, sql, null, null);
        } catch (FlywayBlockStatementExecutionException e) {
            LOG.debug("Statement on line " + sqlStatement.getLineNumber() + " + skipped due to " + e.getMessage());
            return null;
        }

        Results results = sqlStatement.execute(jdbcTemplate, this, config);

        if (results.getException() != null) {
            handleEachMigrateOrUndoStatementCallback(Event.AFTER_EACH_UNDO_STATEMENT_ERROR, Event.AFTER_EACH_MIGRATE_STATEMENT_ERROR, sql, results.getWarnings(), results.getErrors());
            printWarnings(results);
            handleException(results, sqlScript, sqlStatement, config);
            return null;
        }

        handleEachMigrateOrUndoStatementCallback(Event.AFTER_EACH_UNDO_STATEMENT, Event.AFTER_EACH_MIGRATE_STATEMENT, sql, results.getWarnings(), results.getErrors());
        printWarnings(results);
        handleResults(results);
        return results;
    }

    protected void handleResults(Results results) {
        for (Result result : results.getResults()) {
            long updateCount = result.updateCount();
            if (updateCount != -1) {
                handleUpdateCount(updateCount);
            }

            outputQueryResult(result);

        }
    }

    protected void outputQueryResult(Result result) {
        if (outputQueryResults &&
                result.columns() != null && !result.columns().isEmpty()) {
            LOG.info(new AsciiTable(result.columns(), result.data(),
                true, "", "No rows returned").render());
        }
    }

    private void handleUpdateCount(long updateCount) {
        LOG.debug(updateCount + " row" + StringUtils.pluralizeSuffix(updateCount) + " affected");
    }

    protected void handleException(Results results, SqlScript sqlScript, SqlStatement sqlStatement, Configuration config) {




                throw new FlywaySqlScriptException(sqlScript.getResource(), sqlStatement, results.getException(), config.getCurrentEnvironmentName());




    }

    private void printWarnings(Results results) {
        for (Warning warning : results.getWarnings()) {



                if ("00000".equals(warning.getState())) {
                    LOG.info("DB: " + warning.getMessage());
                } else {
                    LOG.warn("DB: " + warning.getMessage()
                                     + " (SQL State: " + warning.getState() + " - Error Code: " + warning.getCode() + ")");
                }



        }
    }

    private void handleEachMigrateOrUndoStatementCallback(Event eventUndo, Event eventMigrate, String sql, List<Warning> warnings, List<Error> errors) {







        callbackExecutor.onEachMigrateOrUndoStatementEvent(eventMigrate, sql, warnings, errors);
    }
}
