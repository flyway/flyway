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
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.errorhandler.Error;
import org.flywaydb.core.api.errorhandler.Context;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.api.errorhandler.Warning;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.ErrorImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sql script containing a series of statements terminated by a delimiter (eg: ;).
 * Single-line (--) and multi-line (/* * /) comments are stripped and ignored.
 */
public class SqlScript {
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

    /**
     * The database-specific support.
     */
    private final Database<?> database;








    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     */
    private final boolean mixed;

    /**
     * The sql statements contained in this script.
     */
    private final List<SqlStatement> sqlStatements;

    /**
     * The resource containing the statements.
     */
    private final Resource resource;

    /**
     * Whether this SQL script contains at least one transactional statement.
     */
    private boolean transactionalStatementFound;

    /**
     * Whether this SQL script contains at least one non-transactional statement.
     */
    private boolean nonTransactionalStatementFound;

    /**
     * Creates a new sql script from this source.
     *
     * @param sqlScriptSource The sql script as a text block with all placeholders already replaced.
     * @param database        The database-specific support.
     */
    public SqlScript(String sqlScriptSource, Database database) {
        this.database = database;
        this.mixed = false;
        this.sqlStatements = parse(sqlScriptSource);
        this.resource = null;



    }

    /**
     * Creates a new sql script from this resource.
     *
     * @param database            The database-specific support.
     * @param sqlScriptResource   The resource containing the statements.
     * @param placeholderReplacer The placeholder replacer.
     * @param encoding            The encoding to use.
     * @param mixed               Whether to allow mixing transactional and non-transactional statements within the same migration.



     */
    public SqlScript(Database database, LoadableResource sqlScriptResource, PlaceholderReplacer placeholderReplacer,
                     String encoding, boolean mixed



    ) {
        this.database = database;
        this.resource = sqlScriptResource;
        this.mixed = mixed;

        String sqlScriptSource = sqlScriptResource.loadAsString(encoding);
        this.sqlStatements = parse(placeholderReplacer.replacePlaceholders(sqlScriptSource));




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
     * For increased testability.
     *
     * @return The sql statements contained in this script.
     */
    public List<SqlStatement> getSqlStatements() {
        return sqlStatements;
    }

    /**
     * @return The resource containing the statements.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Executes this script against the database.
     *
     * @param jdbcTemplate The jdbcTemplate to use to execute this script.
     */
    public void execute(final JdbcTemplate jdbcTemplate) {






        for (SqlStatement sqlStatement : sqlStatements) {
            ContextImpl context = new ContextImpl();

            String sql = sqlStatement.getSql();
            LOG.debug("Executing SQL: " + sql);

            try {
                List<Result> results = sqlStatement.execute(context, jdbcTemplate);



















                printWarnings(context);
                for (Result result : results) {
                    if (result.getUpdateCount() != -1) {
                        LOG.debug("Update Count: " + result.getUpdateCount());
                    }






                }
            } catch (final SQLException e) {










                printWarnings(context);









                throw new FlywaySqlScriptException(resource, sqlStatement, e);
            }
        }
    }





















    private void printWarnings(Context context) {
        for (Warning warning : context.getWarnings()) {
            if ("00000".equals(warning.getState())) {
                LOG.info("DB: " + warning.getMessage());
            } else {
                LOG.warn("DB: " + warning.getMessage()
                        + " (SQL State: " + warning.getState() + " - Error Code: " + warning.getCode() + ")");
            }
        }
    }

    /**
     * Parses this script source into statements.
     *
     * @param sqlScriptSource The script source to parse.
     * @return The parsed statements.
     */
    /* private -> for testing */
    List<SqlStatement> parse(String sqlScriptSource) {
        if (resource != null) {
            LOG.debug("Parsing " + resource.getFilename() + " ...");
        }
        return linesToStatements(readLines(new StringReader(sqlScriptSource)));
    }

    /**
     * Turns these lines in a series of statements.
     *
     * @param lines The lines to analyse.
     * @return The statements contained in these lines (in order).
     */
    /* private -> for testing */
    List<SqlStatement> linesToStatements(List<String> lines) {
        List<SqlStatement> statements = new ArrayList<>();

        Delimiter nonStandardDelimiter = null;
        SqlStatementBuilder sqlStatementBuilder = createSqlStatementBuilder();

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);

            if (sqlStatementBuilder.isEmpty()) {
                if (!StringUtils.hasText(line)) {
                    // Skip empty line between statements.
                    continue;
                }

                Delimiter newDelimiter = sqlStatementBuilder.extractNewDelimiterFromLine(line);
                if (newDelimiter != null) {
                    nonStandardDelimiter = newDelimiter;
                    // Skip this line as it was an explicit delimiter change directive outside of any statements.
                    continue;
                }

                sqlStatementBuilder.setLineNumber(lineNumber);

                // Start a new statement, marking it with this line number.
                if (nonStandardDelimiter != null) {
                    sqlStatementBuilder.setDelimiter(nonStandardDelimiter);
                }
            }

            try {
                sqlStatementBuilder.addLine(line);
            } catch (Exception e) {
                throw new FlywayException("Flyway parsing bug (" + e.getMessage() + ") at line " + lineNumber + ": " + line, e);
            }

            if (sqlStatementBuilder.canDiscard()) {
                sqlStatementBuilder = createSqlStatementBuilder();
            } else if (sqlStatementBuilder.isTerminated()) {
                addStatement(statements, sqlStatementBuilder);
                sqlStatementBuilder = createSqlStatementBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (!sqlStatementBuilder.isEmpty()) {
            addStatement(statements, sqlStatementBuilder);
        }

        return statements;
    }

    protected SqlStatementBuilder createSqlStatementBuilder() {
        return database.createSqlStatementBuilder();
    }

    private void addStatement(List<SqlStatement> statements, SqlStatementBuilder sqlStatementBuilder) {
        SqlStatement sqlStatement = sqlStatementBuilder.getSqlStatement();
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

        LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql() + (sqlStatementBuilder.executeInTransaction() ? "" : " [non-transactional]"));
    }

    /**
     * Parses the textual data provided by this reader into a list of lines.
     *
     * @param reader The reader for the textual data.
     * @return The list of lines (in order).
     * @throws IllegalStateException Thrown when the textual data parsing failed.
     */
    private List<String> readLines(Reader reader) {
        List<String> lines = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            String message = resource == null ?
                    "Unable to parse lines" :
                    "Unable to parse " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")";
            throw new FlywayException(message, e);
        }

        return lines;
    }
}