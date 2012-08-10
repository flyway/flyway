/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.util.ObjectUtils;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

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
    protected static final Delimiter DEFAULT_STATEMENT_DELIMITER = new Delimiter(";", false);

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

    /**
     * Parses this script source into statements.
     *
     * @param sqlScriptSource     The script source to parse.
     * @param placeholderReplacer The placeholder replacer to use.
     * @return The parsed statements.
     */
    /* private -> for testing */
    List<SqlStatement> parse(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        Reader reader = new StringReader(sqlScriptSource);
        List<String> rawLines = readLines(reader);
        List<String> noPlaceholderLines = replacePlaceholders(rawLines, placeholderReplacer);
        List<String> noCommentLines = stripSqlComments(noPlaceholderLines);
        return linesToStatements(noCommentLines);
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

        int statementLineNumber = 0;
        StringBuilder statementSql = new StringBuilder();
        StringBuilder statementSqlWithoutLineBreaks = new StringBuilder();

        Delimiter delimiter = DEFAULT_STATEMENT_DELIMITER;

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            String trimmedLine = line.trim();

            if ((statementSqlWithoutLineBreaks.length() == 0) && !StringUtils.hasText(line)) {
                // Skip empty line between statements.
                continue;
            }

            if ((statementSqlWithoutLineBreaks.length() == 0)) {
                // Start a new statement, marking it with this line number.
                statementLineNumber = lineNumber;
            } else {
                statementSql.append("\n");
                statementSqlWithoutLineBreaks.append(" ");
            }
            statementSql.append(line);
            statementSqlWithoutLineBreaks.append(line.replace("\n", " ").replace("\r", " ").trim());

            if (endsWithOpenMultilineStringLiteral(statementSqlWithoutLineBreaks.toString())) {
                continue;
            }

            Delimiter oldDelimiter = delimiter;
            delimiter = changeDelimiterIfNecessary(statementSqlWithoutLineBreaks, trimmedLine, delimiter);
            if (!ObjectUtils.nullSafeEquals(delimiter, oldDelimiter)) {
                if (isDelimiterChangeExplicit()) {
                    statementSql = new StringBuilder();
                    statementSqlWithoutLineBreaks = new StringBuilder();
                    continue;
                }
            }

            if (lineTerminatesStatement(trimmedLine, delimiter)) {
                String noDelimiterStatementSql = stripDelimiter(statementSql.toString(), delimiter);
                statements.add(new SqlStatement(statementLineNumber, noDelimiterStatementSql));
                LOG.debug("Found statement at line " + statementLineNumber + ": " + statementSql);

                if (!isDelimiterChangeExplicit()) {
                    delimiter = DEFAULT_STATEMENT_DELIMITER;
                }
                statementSql = new StringBuilder();
                statementSqlWithoutLineBreaks = new StringBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (StringUtils.hasText(statementSql.toString())) {
            statements.add(new SqlStatement(statementLineNumber, statementSql.toString()));
        }

        return statements;
    }

    /**
     * Checks whether this line terminates the current statement.
     *
     * @param line      The line to check.
     * @param delimiter The current delimiter.
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    private boolean lineTerminatesStatement(String line, Delimiter delimiter) {
        if (delimiter == null) {
            return false;
        }

        String upperCaseLine = line.toUpperCase();
        String upperCaseDelimiter = delimiter.getDelimiter().toUpperCase();

        if (delimiter.isAloneOnLine() && !upperCaseLine.startsWith(upperCaseDelimiter)) {
            return false;
        }

        return upperCaseLine.endsWith(upperCaseDelimiter);
    }

    /**
     * Checks whether this line in the sql script indicates that the statement delimiter will be different from the
     * current one. Useful for database-specific stored procedures and block constructs.
     *
     * @param statement The statement assembled so far, reduced to a single line with all linebreaks replaced by
     *                  spaces.
     * @param line      The line to analyse.
     * @param delimiter The current delimiter.
     * @return The new delimiter to use (can be the same as the current one) or {@code null} for no delimiter.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Delimiter changeDelimiterIfNecessary(StringBuilder statement, String line, Delimiter delimiter) {
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
     * @return The sql statement without delimiter.
     */
    private static String stripDelimiter(String sql, Delimiter delimiter) {
        return sql.substring(0, sql.toUpperCase().lastIndexOf(delimiter.getDelimiter().toUpperCase()));
    }

    /**
     * Strip single line (--) and multi-line (/* * /) comments from these lines.
     *
     * @param lines The input lines.
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

            noCommentLines.add(line);
        }

        return noCommentLines;
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is a directive that should be processed by the database, {@code false} if not.
     */
    protected boolean isCommentDirective(String line) {
        return false;
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
            throw new IllegalStateException("Cannot parse lines", e);
        }

        return lines;
    }

    /**
     * Replaces the placeholders in these lines with their values.
     *
     * @param lines               The input lines.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
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
     * @return {@code true} if the statement is unfinished and the end is currently in the middle of a multi-line string
     *         literal. {@code false} if not.
     */
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        return false;
    }
}
