/**
 * Copyright 2010-2014 Axel Fontaine
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
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sql script containing a series of statements terminated by semi-columns (;). Single-line (--) and multi-line (/* * /)
 * comments are stripped and ignored.
 */
public class SqlScript {
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

    /**
     * The database-specific support.
     */
    private final DbSupport dbSupport;

    private LineNumberReader sqlScriptReader;

    private List<SqlStatement> sqlStatements;

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource The sql script as a text block with all placeholders already replaced.
     * @param dbSupport       The database-specific support.
     */
    public SqlScript(String sqlScriptSource, DbSupport dbSupport) {
        this.dbSupport = dbSupport;
        LineNumberReader sqlScriptReader = stringToReader(sqlScriptSource);
        try {
            sqlStatements = parse(sqlScriptReader, -1);
        } finally {
            if (sqlScriptReader != null) {
                try {
                    sqlScriptReader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public SqlScript(LineNumberReader sqlScriptReader, DbSupport dbSupport) {
        this.dbSupport = dbSupport;
        this.sqlScriptReader = sqlScriptReader;
    }

    /**
     * Dummy constructor to increase testability.
     *
     * @param dbSupport The database-specific support.
     */
    SqlScript(DbSupport dbSupport) {
        this.dbSupport = dbSupport;
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
     * Executes this script against the database.
     *
     * @param jdbcTemplate The jdbc template to use to execute this script.
     */
    public void execute(final JdbcTemplate jdbcTemplate) {
        execute(jdbcTemplate, this.sqlStatements);
    }

    public void execute(final JdbcTemplate jdbcTemplate, List<SqlStatement> sqlStmts) {
        for (SqlStatement sqlStatement : sqlStmts) {
            String sql = sqlStatement.getSql();
            LOG.debug("Executing SQL: " + sql);

            try {
                if (sqlStatement.isPgCopy()) {
                    dbSupport.executePgCopy(jdbcTemplate.getConnection(), sql);
                } else {
                    jdbcTemplate.executeStatement(sql);
                }
            } catch (SQLException e) {
                throw new FlywaySqlScriptException(sqlStatement.getLineNumber(), sql, e);
            }
        }
    }

    public void executeBatch(JdbcTemplate jdbcTemplate, int chunkSize) {
        List<SqlStatement> sqlStmts;

        if (this.sqlScriptReader == null) {
            throw new FlywayException("sqlScriptReader not initialise");
        }

        try {
            while (!(sqlStmts = parse(this.sqlScriptReader, chunkSize)).isEmpty()) {
                execute(jdbcTemplate, sqlStmts);
            }
        } finally {
            if (this.sqlScriptReader != null) {
                try {
                    this.sqlScriptReader.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public List<SqlStatement> parse(String source) {
        return parse(stringToReader(source), -1);
    }

    List<SqlStatement> parse(LineNumberReader reader, int chunk) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        boolean inMultilineComment = false;
        Delimiter nonStandardDelimiter = null;
        SqlStatementBuilder sqlStatementBuilder = dbSupport.createSqlStatementBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {

                if (sqlStatementBuilder.isEmpty()) {
                    if (!StringUtils.hasText(line)) {
                        // Skip empty line between statements.
                        continue;
                    }

                    String trimmedLine = line.trim();

                    if (!sqlStatementBuilder.isCommentDirective(trimmedLine)) {
                        if (trimmedLine.startsWith("/*")) {
                            inMultilineComment = true;
                        }

                        if (inMultilineComment) {
                            if (trimmedLine.endsWith("*/")) {
                                inMultilineComment = false;
                            }
                            // Skip line part of a multi-line comment
                            continue;
                        }

                        if (sqlStatementBuilder.isSingleLineComment(trimmedLine)) {
                            // Skip single-line comment
                            continue;
                        }
                    }

                    Delimiter newDelimiter = sqlStatementBuilder.extractNewDelimiterFromLine(line);
                    if (newDelimiter != null) {
                        nonStandardDelimiter = newDelimiter;
                        // Skip this line as it was an explicit delimiter change directive outside of any statements.
                        continue;
                    }

                    sqlStatementBuilder.setLineNumber(reader.getLineNumber());

                    // Start a new statement, marking it with this line number.
                    if (nonStandardDelimiter != null) {
                        sqlStatementBuilder.setDelimiter(nonStandardDelimiter);
                    }
                }

                sqlStatementBuilder.addLine(line);

                if (sqlStatementBuilder.isTerminated()) {
                    SqlStatement sqlStatement = sqlStatementBuilder.getSqlStatement();
                    statements.add(sqlStatement);
                    LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql());

                    if (chunk == statements.size()) {
                        return statements;
                    }

                    sqlStatementBuilder = dbSupport.createSqlStatementBuilder();
                }
            }
        } catch (IOException e) {
            throw new FlywayException("Unable to load resource", e);
        }

        // Catch any statements not followed by delimiter.
        if (!sqlStatementBuilder.isEmpty()) {
            statements.add(sqlStatementBuilder.getSqlStatement());
        }

        return statements;
    }

    private LineNumberReader stringToReader(String sqlScriptSource) {
        return new LineNumberReader(new StringReader(sqlScriptSource));
    }


}
