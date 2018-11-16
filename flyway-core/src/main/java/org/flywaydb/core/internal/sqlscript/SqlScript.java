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
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.line.LineReader;
import org.flywaydb.core.internal.line.PlaceholderReplacingLine;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SQL script containing a series of statements terminated by a delimiter (eg: ;).
 * Single-line (--) and multi-line (/* * /) comments are stripped and ignored.
 */
public class SqlScript implements Comparable<SqlScript> {
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

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
    private final List<SqlStatement> sqlStatements;

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
     * Creates a new sql script from this source.
     *
     * @param resource The sql script resource.
     * @param mixed    Whether to allow mixing transactional and non-transactional statements within the same migration.
     */
    public SqlScript(SqlStatementBuilderFactory sqlStatementBuilderFactory, LoadableResource resource, boolean mixed) {
        this.resource = resource;
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
    private List<SqlStatement> extractStatements(LineReader reader) {
        Line line;

        List<SqlStatement> statements = new ArrayList<>();

        Delimiter nonStandardDelimiter = null;
        SqlStatementBuilder sqlStatementBuilder = sqlStatementBuilderFactory.createSqlStatementBuilder();

        while ((line = reader.readLine()) != null) {
            line = new PlaceholderReplacingLine(line, sqlStatementBuilderFactory.getPlaceholderReplacer());
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

        LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                + (sqlStatementBuilder.executeInTransaction() ? "" : " [non-transactional]"));
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

    @Override
    public int compareTo(SqlScript o) {
        return resource.getRelativePath().compareTo(o.resource.getRelativePath());
    }
}