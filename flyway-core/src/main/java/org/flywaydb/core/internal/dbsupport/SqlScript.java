/**
 * Copyright 2010-2016 Boxfuse GmbH
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
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
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
     * The sql statements contained in this script.
     */
    private final List<SqlStatement> sqlStatements;

    /**
     * The resource containing the statements.
     */
    private final Resource resource;

    /**
     * Creates a new sql script from this source.
     *
     * @param sqlScriptSource The sql script as a text block with all placeholders already replaced.
     * @param dbSupport       The database-specific support.
     */
    public SqlScript(String sqlScriptSource, DbSupport dbSupport) {
        this.dbSupport = dbSupport;
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
     */
    public SqlScript(DbSupport dbSupport, Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding) {
        this.dbSupport = dbSupport;

        String sqlScriptSource = sqlScriptResource.loadAsString(encoding);
        this.sqlStatements = parse(placeholderReplacer.replacePlaceholders(sqlScriptSource));

        this.resource = sqlScriptResource;
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
                if (sqlStatement.isPgCopy()) {
                    dbSupport.executePgCopy(jdbcTemplate.getConnection(), sql);
                } else {
                    jdbcTemplate.executeStatement(sql);
                }
            } catch (SQLException e) {
                throw new FlywaySqlScriptException(resource, sqlStatement, e);
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

            sqlStatementBuilder.addLine(line);

            if (sqlStatementBuilder.canDiscard()) {
                sqlStatementBuilder = dbSupport.createSqlStatementBuilder();
            } else if (sqlStatementBuilder.isTerminated()) {
                SqlStatement sqlStatement = sqlStatementBuilder.getSqlStatement();
                statements.add(sqlStatement);
                LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql());

                sqlStatementBuilder = dbSupport.createSqlStatementBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (!sqlStatementBuilder.isEmpty()) {
            statements.add(sqlStatementBuilder.getSqlStatement());
        }

        return statements;
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
