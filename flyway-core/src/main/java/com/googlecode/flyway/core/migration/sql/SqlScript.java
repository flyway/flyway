/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.migration.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Sql script containing a series of statements terminated by semi-columns (;). Single-line (--) and multi-line (/* * /)
 * comments are stripped and ignored.
 */
public class SqlScript {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SqlScript.class);

    /**
     * The default Statement delimiter.
     */
    protected static final String DEFAULT_STATEMENT_DELIMITER = ";";

    /**
     * The sql statements contained in this script.
     */
    private final List<SqlStatement> sqlStatements;

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     */
    public SqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        this.sqlStatements = parse(sqlScriptSource, placeholderReplacer);
    }

    /**
     * Creates a new SqlScript with these statements and this name.
     *
     * @param sqlStatements The statements of the script.
     */
    public SqlScript(List<SqlStatement> sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    /**
     * Dummy constructor to increase testability.
     */
    protected SqlScript() {
        sqlStatements = null;
    }

    /**
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
        for (SqlStatement sqlStatement : sqlStatements) {
            sqlStatement.execute(jdbcTemplate);
        }
    }

    private List<SqlStatement> parse(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        Reader reader = new StringReader(sqlScriptSource);
        List<String> rawLines = readLines(reader);

        List<String> trimmedLines = trimLines(rawLines);
        List<String> noCommentLines = stripSqlComments(trimmedLines);
        List<String> noPlaceholderLines = replacePlaceholders(noCommentLines, placeholderReplacer);
        return linesToStatements(noPlaceholderLines);
    }


    /**
     * Turns these lines in a series of statements.
     *
     * @param lines The lines to analyse.
     *
     * @return The statements contained in these lines (in order).
     */
    /* private -> for testing */
    List<SqlStatement> linesToStatements(List<String> lines) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        int statementLineNumber = 0;
        String statementSql = "";

        String delimiter = DEFAULT_STATEMENT_DELIMITER;

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);

            if (!StringUtils.hasText(line)) {
                continue;
            }

            if (!StringUtils.hasText(statementSql)) {
                statementLineNumber = lineNumber;
            } else {
                statementSql += "\n";
            }
            statementSql += line;

            String statementSqlWithoutLineBreaks = statementSql.replaceAll("\n", " ").replaceAll("\r", " ");
            if (endsWithOpenMultilineStringLiteral(statementSqlWithoutLineBreaks)) {
                continue;
            }

            String oldDelimiter = delimiter;
            delimiter = changeDelimiterIfNecessary(statementSqlWithoutLineBreaks, line, delimiter);
            if (!ObjectUtils.nullSafeEquals(delimiter, oldDelimiter)) {
                if (isDelimiterChangeExplicit()) {
                    statementSql = "";
                    continue;
                }
            }

            if ((delimiter != null) && line.toUpperCase().endsWith(delimiter.toUpperCase())) {
                String noDelimiterStatementSql = stripDelimiter(statementSql, delimiter);
                statements.add(new SqlStatement(statementLineNumber, noDelimiterStatementSql));
                LOG.debug("Found statement at line " + statementLineNumber + ": " + statementSql);

                if (!isDelimiterChangeExplicit()) {
                    delimiter = DEFAULT_STATEMENT_DELIMITER;
                }
                statementSql = "";
            }
        }

        // Catch any statements not followed by delimiter.
        if (StringUtils.hasText(statementSql)) {
            statements.add(new SqlStatement(statementLineNumber, statementSql));
        }

        return statements;
    }

    /**
     * Checks whether this line in the sql script indicates that the statement delimiter will be different from the
     * current one. Useful for database-specific stored procedures and block constructs.
     *
     * @param statement The statement assembled so far, reduced to a single line with all linebreaks replaced by
     *                  spaces.
     * @param line      The line to analyse.
     * @param delimiter The current delimiter.
     *
     * @return The new delimiter to use (can be the same as the current one) or {@code null} for no delimiter.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter) {
        return delimiter;
    }

    /**
     * @return {@code true} if this database uses an explicit delimiter change statement. {@code false} if a delimiter
     *         change is implied by certain statements.
     */
    protected boolean isDelimiterChangeExplicit() {
        return false;
    }

    /**
     * Strips this delimiter from this sql statement.
     *
     * @param sql       The statement to parse.
     * @param delimiter The delimiter to strip.
     *
     * @return The sql statement without delimiter.
     */
    private static String stripDelimiter(String sql, String delimiter) {
        return sql.substring(0, sql.length() - delimiter.length());
    }

    /**
     * Strip single line (--) and multi-line (/* * /) comments from these lines.
     *
     * @param lines The input lines.
     *
     * @return The input lines, trimmed of leading and trailing whitespace, with the comments lines left blank.
     */
    /* private -> for testing */
    List<String> stripSqlComments(List<String> lines) {
        List<String> noCommentLines = new ArrayList<String>(lines.size());

        boolean inMultilineComment = false;
        for (String line : lines) {
            String trimmedLine = line.trim();

            if (!isCommentDirective(trimmedLine)) {
                if (trimmedLine.startsWith("--")) {
                    noCommentLines.add("");
                    continue;
                }

                if (trimmedLine.startsWith("/*")) {
                    inMultilineComment = true;
                }

                if (inMultilineComment) {
                    if (trimmedLine.endsWith("*/")) {
                        inMultilineComment = false;
                    }
                    noCommentLines.add("");
                    continue;
                }
            }

            noCommentLines.add(trimmedLine);
        }

        return noCommentLines;
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     *
     * @param line The line to analyse.
     *
     * @return {@code true} if it is a directive that should be processed by the database, {@code false} if not.
     */
    protected boolean isCommentDirective(String line) {
        return false;
    }

    /**
     * Trims these lines of leading and trailing whitespace.
     *
     * @param lines The input lines.
     *
     * @return The input lines, trimmed of leading and trailing whitespace.
     */
    private List<String> trimLines(List<String> lines) {
        List<String> trimmedLines = new ArrayList<String>(lines.size());

        for (String line : lines) {
            String trimmedLine = line.trim();
            trimmedLines.add(trimmedLine);
        }

        return trimmedLines;
    }

    /**
     * Parses the textual data provided by this reader into a list of lines.
     *
     * @param reader The reader for the textual data.
     *
     * @return The list of lines (in order).
     *
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
            throw new IllegalStateException("Cannot parse lines", e);
        }

        return lines;
    }

    /**
     * Replaces the placeholders in these lines with their values.
     *
     * @param lines               The input lines.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     *
     * @return The lines with placeholders replaced.
     */
    private List<String> replacePlaceholders(List<String> lines, PlaceholderReplacer placeholderReplacer) {
        List<String> noPlaceholderLines = new ArrayList<String>(lines.size());

        for (String line : lines) {
            noPlaceholderLines.add(placeholderReplacer.replacePlaceholders(line));
        }

        return noPlaceholderLines;
    }

    /**
     * Checks whether the statement we have assembled so far ends with an open multi-line string literal (which will be
     * continued on the next line).
     *
     * @param statement The current statement, assembled from the lines we have parsed so far. May not yet be complete.
     *
     * @return {@code true} if the statement is unfinished and the end is currently in the middle of a multi-line string
     *         literal. {@code false} if not.
     */
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        return false;
    }
}
