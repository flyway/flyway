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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting PostgreSQL specific syntax.
 */
public class PostgreSQLSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    /*private -> for testing*/
    static final Pattern DOLLAR_QUOTE_REGEX = Pattern.compile("(\\$[A-Za-z0-9_]*\\$).*");

    private static final Pattern CREATE_DATABASE_TABLESPACE_SUBSCRIPTION_REGEX = Pattern.compile("^(CREATE|DROP) (DATABASE|TABLESPACE|SUBSCRIPTION) .*");
    private static final Pattern ALTER_SYSTEM_REGEX = Pattern.compile("^ALTER SYSTEM .*");
    private static final Pattern CREATE_INDEX_CONCURRENTLY_REGEX = Pattern.compile("^(CREATE|DROP)( UNIQUE)? INDEX CONCURRENTLY .*");
    private static final Pattern REINDEX_REGEX = Pattern.compile("^REINDEX( VERBOSE)? (SCHEMA|DATABASE|SYSTEM) .*");
    private static final Pattern VACUUM_REGEX = Pattern.compile("^VACUUM .*");
    private static final Pattern DISCARD_ALL_REGEX = Pattern.compile("^DISCARD ALL .*");
    private static final Pattern ALTER_TYPE_ADD_VALUE_REGEX = Pattern.compile("^ALTER TYPE .* ADD VALUE .*");
    private static final Pattern COPY_REGEX = Pattern.compile("^COPY|COPY\\s.*");
    private static final Pattern CREATE_RULE_FULL_REGEX = Pattern.compile("^CREATE( OR REPLACE)? RULE .* DO (ALSO|INSTEAD) \\(.*;\\s?\\)\\s?;\\s?");
    private static final Pattern CREATE_RULE_FULL_SINGLE_REGEX = Pattern.compile("^CREATE( OR REPLACE)? RULE .* DO (ALSO|INSTEAD) \\([^;]+\\)\\s?;\\s?");
    private static final Pattern CREATE_RULE_PARTIAL_REGEX = Pattern.compile("^CREATE( OR REPLACE)? RULE .* DO (ALSO|INSTEAD) \\(.*");

    /**
     * The copy statement seen so far.
     */
    private String copyStatement;

    /**
     * Whether this statement is a COPY statement.
     */
    private boolean pgCopy;

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    public PostgreSQLSqlStatementBuilder() {
        super(Delimiter.SEMICOLON);
    }

    /**
     * @return The assembled statement, with the delimiter stripped off.
     */
    @SuppressWarnings("unchecked")
    @Override
    public SqlStatement getSqlStatement() {
        if (pgCopy) {
            return new PostgreSQLCopyStatement(lines.subList(firstNonCommentLine, lines.size()));
        }
        return super.getSqlStatement();
    }

    @Override
    protected void applyStateChanges(String line) {
        if (pgCopy) {
            return;
        }

        super.applyStateChanges(line);

        if (!executeInTransaction || !hasNonCommentPart()) {
            return;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 256) {
            statementStart += line;
            statementStart += " ";
            statementStart = StringUtils.collapseWhitespace(statementStart);
        }

        if (CREATE_DATABASE_TABLESPACE_SUBSCRIPTION_REGEX.matcher(statementStart).matches()
                || ALTER_SYSTEM_REGEX.matcher(statementStart).matches()
                || CREATE_INDEX_CONCURRENTLY_REGEX.matcher(statementStart).matches()
                || REINDEX_REGEX.matcher(statementStart).matches()
                || VACUUM_REGEX.matcher(statementStart).matches()
                || DISCARD_ALL_REGEX.matcher(statementStart).matches()
                || ALTER_TYPE_ADD_VALUE_REGEX.matcher(statementStart).matches()
        ) {
            executeInTransaction = false;
        }
    }

    @Override
    protected Collection<String> tokenizeLine(String line) {
        return StringUtils.tokenizeToStringCollection(line, " @<>;:=|(),+{}[]");
    }

    @Override
    protected String simplifyLine(String line) {
        return super.simplifyLine(line.replace("$$", " $$ "));
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        Matcher matcher = DOLLAR_QUOTE_REGEX.matcher(token);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (pgCopy) {
            return PostgreSQLCopyStatement.COPY_DELIMITER;
        }

        if (COPY_REGEX.matcher(line).matches()) {
            copyStatement = line;
        } else if (copyStatement != null) {
            copyStatement += " " + line;
        }

        if (copyStatement != null && copyStatement.contains(" FROM STDIN")) {
            pgCopy = true;
            return PostgreSQLCopyStatement.COPY_DELIMITER;
        }

        if (CREATE_RULE_FULL_SINGLE_REGEX.matcher(statementStart).matches()) {
            return Delimiter.SEMICOLON;
        }

        if (CREATE_RULE_FULL_REGEX.matcher(statementStart).matches()) {
            return Delimiter.SEMICOLON;
        }

        if (CREATE_RULE_PARTIAL_REGEX.matcher(statementStart).matches()) {
            return null;
        }

        return delimiter;
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("E'")) {
            return token.substring(token.indexOf("'"));
        }

        return token;
    }
}