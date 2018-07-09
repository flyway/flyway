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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.ErrorImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;
import org.flywaydb.core.internal.util.jdbc.StandardContext;
import org.flywaydb.core.internal.util.line.Line;
import org.flywaydb.core.internal.util.line.LineReader;
import org.flywaydb.core.internal.util.line.PlaceholderReplacingLine;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL script containing a series of statements terminated by a delimiter (eg: ;).
 * Single-line (--) and multi-line (/* * /) comments are stripped and ignored.
 */
public class SqlScript<C extends StandardContext> {
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

    /**
     * The configuration to use.
     */
    protected final Configuration configuration;

    /**
     * Factory for creating SQL statement builders.
     */
    private final SqlStatementBuilderFactory sqlStatementBuilderFactory;




























    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     */
    private final boolean mixed;

    /**
     * The sql statements contained in this script.
     */
    private List<SqlStatement<C>> sqlStatements;

    /**
     * Whether this SQL script contains at least one transactional statement.
     */
    private boolean transactionalStatementFound;

    /**
     * Whether this SQL script contains at least one non-transactional statement.
     */
    private boolean nonTransactionalStatementFound;

    /**
     * The resource containing the statements.
     */
    protected final LoadableResource resource;

    /**
     * The placeholder replacer.
     */
    protected final PlaceholderReplacer placeholderReplacer;

    /**
     * Creates a new sql script from this source.
     *
     * @param configuration              The configuration to use.
     * @param resource                   The sql script resource.
     * @param mixed                      Whether to allow mixing transactional and non-transactional statements within the same migration.




     * @param placeholderReplacer        The placeholder replacer to use.
     */
    public SqlScript(Configuration configuration, SqlStatementBuilderFactory sqlStatementBuilderFactory,
                     LoadableResource resource, boolean mixed




            , PlaceholderReplacer placeholderReplacer
    ) {
        this.resource = resource;
        this.placeholderReplacer = placeholderReplacer;
        this.configuration = configuration;
        this.sqlStatementBuilderFactory = sqlStatementBuilderFactory;
        this.mixed = mixed;








        LOG.debug("Parsing " + resource.getFilename() + " ...");
        LineReader reader = null;
        try {
            reader = resource.loadAsString();
            this.sqlStatements = extractStatements(reader);
        } finally {
            IOUtils.close(reader);
        }
    }

    /**
     * Parses the textual data provided by this reader into a list of statements.
     *
     * @param reader The reader for the textual data.
     * @return The list of statements (in order).
     * @throws IllegalStateException Thrown when the textual data parsing failed.
     */
    private List<SqlStatement<C>> extractStatements(LineReader reader) {
        Line line;

        List<SqlStatement<C>> statements = new ArrayList<>();

        Delimiter nonStandardDelimiter = null;
        SqlStatementBuilder sqlStatementBuilder = sqlStatementBuilderFactory.createSqlStatementBuilder();

        while ((line = reader.readLine()) != null) {
            line = new PlaceholderReplacingLine(line, placeholderReplacer);
            String lineStr = line.getLine();
            if (sqlStatementBuilder.isEmpty() && !StringUtils.hasText(lineStr)) {
                // Skip empty line between statements.
                continue;
            }

            if (!sqlStatementBuilder.hasNonCommentPart()) {
                Delimiter newDelimiter = sqlStatementBuilder.extractNewDelimiterFromLine(lineStr);
                if (newDelimiter != null) {
                    nonStandardDelimiter = newDelimiter;
                    // Skip this line as it was an explicit delimiter change directive outside of any statements.
                    continue;
                }

                // Start a new statement, marking it with this line number.
                if (nonStandardDelimiter != null) {
                    sqlStatementBuilder.setDelimiter(nonStandardDelimiter);
                }
            }

            try {
                sqlStatementBuilder.addLine(line);
            } catch (Exception e) {
                throw new FlywayException("Flyway parsing bug (" + e.getMessage() + ") at line " + line.getLineNumber() + ": " + lineStr, e);
            }

            if (sqlStatementBuilder.canDiscard()) {
                sqlStatementBuilder = sqlStatementBuilderFactory.createSqlStatementBuilder();
            } else if (sqlStatementBuilder.isTerminated()) {
                addStatement(statements, sqlStatementBuilder);
                sqlStatementBuilder = sqlStatementBuilderFactory.createSqlStatementBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (!sqlStatementBuilder.isEmpty() && sqlStatementBuilder.hasNonCommentPart()) {
            addStatement(statements, sqlStatementBuilder);
        }

        return statements;
    }

    private void addStatement(List<SqlStatement<C>> statements, SqlStatementBuilder sqlStatementBuilder) {
        SqlStatement<C> sqlStatement = sqlStatementBuilder.getSqlStatement();
        statements.add(sqlStatement);

        if (sqlStatementBuilder.executeInTransaction()) {
            transactionalStatementFound = true;
        } else {
            nonTransactionalStatementFound = true;
        }

        if (!mixed && transactionalStatementFound && nonTransactionalStatementFound) {
            throw new FlywayException(
                    "Detected both transactional and non-transactional statements within the same migration"
                            + " (even though mixed is false). Offending statement found at line "
                            + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                            + (sqlStatementBuilder.executeInTransaction() ? "" : " [non-transactional]"));
        }

        LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                + (sqlStatementBuilder.executeInTransaction() ? "" : " [non-transactional]"));
    }

    /**
     * For increased testability.
     *
     * @return The sql statements contained in this script.
     */
    public List<SqlStatement<C>> getSqlStatements() {
        return sqlStatements;
    }

    /**
     * @return The resource containing the statements.
     */
    public final LoadableResource getResource() {
        return resource;
    }

    /**
     * Whether the execution should take place inside a transaction. This is useful for databases
     * like PostgreSQL where certain statement can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    public boolean executeInTransaction() {
        return !nonTransactionalStatementFound;
    }

    /**
     * Executes this script against the database.
     *
     * @param jdbcTemplate The jdbcTemplate to use to execute this script.
     */
    public void execute(final JdbcTemplate jdbcTemplate) {









        for (int i = 0; i < getSqlStatements().size(); i++) {
            SqlStatement<C> sqlStatement = getSqlStatements().get(i);
            String sql = sqlStatement.getSql();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing "



                        + "SQL: " + sql);
            }














                executeStatement(jdbcTemplate, sqlStatement);



        }
    }

















































    private void executeStatement(JdbcTemplate jdbcTemplate, SqlStatement<C> sqlStatement) {
        C context = createContext();

        String sql = sqlStatement.getSql() + sqlStatement.getDelimiter();
        try {






            List<Result> results = sqlStatement.execute(context, jdbcTemplate);







            printWarnings(context);
            handleResults(context, results);
        } catch (final SQLException e) {










            printWarnings(context);





            handleException(e, sqlStatement, context);
        }
    }

    private void handleResults(C context, List<Result> results) {
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

    protected void handleException(SQLException e, SqlStatement sqlStatement, C context) {
        throw new FlywaySqlScriptException(resource, sqlStatement, e);
    }

    protected C createContext() {
        //noinspection unchecked
        return (C) new StandardContext();
    }






    private void printWarnings(C context) {
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