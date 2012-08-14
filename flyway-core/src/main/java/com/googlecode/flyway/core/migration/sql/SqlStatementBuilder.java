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

/**
 * Builds a SQL statement, one line at a time.
 */
public class SqlStatementBuilder {
    /**
     * The current statement, as it is being built.
     */
    private StringBuilder statement = new StringBuilder();

    /**
     * A simplified version of the current statement, to facilitate parsing.
     */
    private final StringBuilder statementSimplified = new StringBuilder();

    /**
     * The initial line number of this statement.
     */
    private int lineNumber;

    /**
     * Flag indicating whether the current statement is properly terminated.
     */
    private boolean terminated;

    /**
     * The current delimiter to look for to terminate the statement.
     */
    private Delimiter delimiter = getDefaultDelimiter();

    /**
     * @return The default delimiter for this database.
     */
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter(";", false);
    }

    /**
     * @param lineNumber The initial line number of this statement.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * @param delimiter The current delimiter to look for to terminate the statement.
     */
    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Checks whether the statement is still empty.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isEmpty() {
        return statementSimplified.length() == 0;
    }

    /**
     * Checks whether the statement being built is now properly terminated.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @return The assembled statement, with the delimiter stripped off.
     */
    public SqlStatement getSqlStatement() {
        return new SqlStatement(lineNumber, statement.toString());
    }

    /**
     * Analyses this line ánd extracts the new default delimiter.
     * This method is only called between statements and looks for explicit delimiter change directives.
     *
     * @return The new delimiter. {@code null} if it is the same as the current one.
     */
    @SuppressWarnings("UnusedParameters")
    public Delimiter extractNewDelimiterFromLine(String line) {
        return null;
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is a directive that should be processed by the database, {@code false} if not.
     */
    public boolean isCommentDirective(String line) {
        return false;
    }

    /**
     * Adds this line to the current statement being built.
     *
     * @param line The line to add.
     */
    public void addLine(String line) {
        if (!isEmpty()) {
            statement.append("\n");
            statementSimplified.append(" ");
        }

        statement.append(line);

        //TODO: Check if replace operations actually have an effect
        statementSimplified.append(line.replace("\n", " ").replace("\r", " ").trim());

        if (endsWithOpenMultilineStringLiteral(statementSimplified.toString())) {
            return;
        }

        String trimmedLine = line.trim();

        Delimiter oldDelimiter = delimiter;
        delimiter = changeDelimiterIfNecessary(statementSimplified, trimmedLine, delimiter);
        if (!ObjectUtils.nullSafeEquals(delimiter, oldDelimiter)) {
            if (isDelimiterChangeExplicit()) {
                terminated = true;
            }
        }

        if (lineTerminatesStatement(trimmedLine, delimiter)) {
            //TODO: Check if the delimiter can be stripped from the existing statement instead
            statement = new StringBuilder(stripDelimiter(statement.toString(), delimiter));
            terminated = true;
        }
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
     * Strips this delimiter from this sql statement.
     *
     * @param sql       The statement to parse.
     * @param delimiter The delimiter to strip.
     * @return The sql statement without delimiter.
     */
    private static String stripDelimiter(String sql, Delimiter delimiter) {
        return sql.substring(0, sql.toUpperCase().lastIndexOf(delimiter.getDelimiter().toUpperCase()));
    }
}
