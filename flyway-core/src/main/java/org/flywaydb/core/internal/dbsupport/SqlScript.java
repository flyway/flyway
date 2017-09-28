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
package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.api.FlywayException;


import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
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
    private final DbSupport dbSupport;








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
     * Whether exceptions in the subsequent statements should cause the migration to fail.
     */
    private boolean failOnException;

    /**
     * Creates a new sql script from this source.
     *
     * @param sqlScriptSource The sql script as a text block with all placeholders already replaced.
     * @param dbSupport       The database-specific support.
     */
    public SqlScript(String sqlScriptSource, DbSupport dbSupport) {
        this.dbSupport = dbSupport;
        this.mixed = false;
        this.failOnException = true;
        this.sqlStatements = parse(sqlScriptSource);
        this.resource = null;


    }

    /**
     * Creates a new sql script from this resource.
     *
     * @param dbSupport           The database-specific support.
     * @param sqlScriptResource   The resource containing the statements.
     * @param placeholderReplacer The placeholder replacer.
     * @param encoding            The encoding to use.
     * @param mixed               Whether to allow mixing transactional and non-transactional statements within the same migration.



     */
    public SqlScript(DbSupport dbSupport, LoadableResource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding, boolean mixed

    ) {
        this.dbSupport = dbSupport;
        this.resource = sqlScriptResource;
        this.mixed = mixed;
        this.failOnException = true;

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
     * @param jdbcTemplate The jdbc template to use to execute this script.
     */
    public void execute(final JdbcTemplate jdbcTemplate) {
        for (SqlStatement sqlStatement : sqlStatements) {
            String sql = sqlStatement.getSql();
            LOG.debug("Executing SQL: " + sql);

            try {
                sqlStatement.execute(jdbcTemplate);
            } catch (final SQLException e) {
                if (sqlStatement.getFailOnException()) {
                    throw new FlywaySqlScriptException(resource, sqlStatement, e);
                } else {
                    LOG.warn(createExceptionWarning(resource, sqlStatement, e));
                }
            }
        }
    }

    private String createExceptionWarning(Resource resource, SqlStatement statement, SQLException e) {
        StringBuilder msg = new StringBuilder("Statement failed");
        String underline = StringUtils.trimOrPad("", msg.length(), '-');
        msg.append("\n");
        msg.append(underline);
        msg.append("\n");

        SQLException rootCause = e;
        while (rootCause.getNextException() != null) {
            rootCause = rootCause.getNextException();
        }

        msg.append("SQL State  : " + rootCause.getSQLState() + "\n");
        msg.append("Error Code : " + rootCause.getErrorCode() + "\n");
        if (rootCause.getMessage() != null) {
            msg.append("Message    : " + rootCause.getMessage().trim() + "\n");
        }

        if (resource != null) {
            msg.append("Location   : " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")\n");
        }
        if (statement != null) {
            msg.append("Line       : " + statement.getLineNumber() + "\n");
            msg.append("Statement  : " + statement.getSql() + "\n");
        }
        return msg.toString();
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
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        Delimiter nonStandardDelimiter = null;
        SqlStatementBuilder sqlStatementBuilder = dbSupport.createSqlStatementBuilder();

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
                throw new FlywayException("Flyway parsing bug (" + e.getMessage() + ") at line " + lineNumber + ": " + line);
            }

            if (sqlStatementBuilder.canDiscard()) {
                sqlStatementBuilder = dbSupport.createSqlStatementBuilder();
            } else if (sqlStatementBuilder.isTerminated()) {
                addStatement(statements, sqlStatementBuilder);
                sqlStatementBuilder = dbSupport.createSqlStatementBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (!sqlStatementBuilder.isEmpty()) {
            addStatement(statements, sqlStatementBuilder);
        }

        return statements;
    }

    private void addStatement(List<SqlStatement> statements, SqlStatementBuilder sqlStatementBuilder) {
        if (isExceptionDirective(sqlStatementBuilder)) {
            processExceptionDirective(sqlStatementBuilder);
            return;
        }

        SqlStatement sqlStatement = sqlStatementBuilder.getSqlStatement();
        sqlStatement.setFailOnException(failOnException);
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
     * Determines if this statement is an exception directive.
     *
     * An exception directive is a statement whose only purpose is to instruct flyway how to handle exceptions
     * for all subsequent statements.
     *
     * @param sqlStatementBuilder the statement to evaluate
     * @return {@code true} if this statement is an exception directive, {@code false} otherwise.
     */
    private boolean isExceptionDirective(SqlStatementBuilder sqlStatementBuilder) {
        if (sqlStatementBuilder.isIgnoreExceptionDirective() || sqlStatementBuilder.isFailOnExceptionDirective()) {
            return true;
        }
        return false;
    }

    /**
     * Records the instructions from an exception directive.
     *
     * An exception directive is a statement whose only purpose is to instruct flyway how to handle exceptions
     * for all subsequent statements.
     *
     * @param sqlStatementBuilder the exception directive statement.
     */
    private void processExceptionDirective(SqlStatementBuilder sqlStatementBuilder) {
        if (sqlStatementBuilder.isIgnoreExceptionDirective()) {
            failOnException = false;
        } else if (sqlStatementBuilder.isFailOnExceptionDirective()) {
            failOnException = true;
        }
    }

    /**
     * Parses the textual data provided by this reader into a list of lines.
     *
     * @param reader The reader for the textual data.
     * @return The list of lines (in order).
     * @throws IllegalStateException Thrown when the textual data parsing failed.
     */
    private List<String> readLines(Reader reader) {
        List<String> lines = new ArrayList<String>();

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