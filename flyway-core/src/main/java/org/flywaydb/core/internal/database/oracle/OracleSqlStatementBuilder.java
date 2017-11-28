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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatement;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting Oracle-specific PL/SQL constructs.
 */
public class OracleSqlStatementBuilder extends SqlStatementBuilder {
    private static final Log LOG = LogFactory.getLog(SqlStatementBuilder.class);

    /**
     * Regex for keywords that can appear before a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX = Pattern.compile("^(N|IF|ELSIF|SELECT|IMMEDIATE|RETURN|IS)('.*)");

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(USING|THEN|FROM|AND|OR|AS)(?!.)");

    // [pro]
    private static Pattern toRegex(String... commands) {
        return Pattern.compile("(" + StringUtils.arrayToDelimitedString("|", commands) + ")(\\s.*)?");
    }

    private static final String UNSUPPORTED_SQLPLUS_COMMANDS =
            "ACC|ACCEPT|" +
                    "A|APPEND|" +
                    "ARCHIVE|" +
                    "ATTR|ATTRIBUTE|" +
                    "BRE|BREAK|" +
                    "BTI|BTITLE|" +
                    "C|CHANGE|" +
                    "CL|CLEAR|" +
                    "COL|COLUMN|" +
                    "COMP|COMPUTE|" +
                    "CONN|CONNECT|" +
                    "COPY|" +
                    "DEF|DEFINE|" +
                    "DEL|" +
                    "DESC|DESCRIBE|" +
                    "DISC|DISCONNECT|" +
                    "ED|EDIT|" +
                    "EXIT|" +
                    "GET|" +
                    "HELP|" +
                    "HIST|HISTORY|" +
                    "HO|HOST|" +
                    "I|INPUT|" +
                    "L|LIST|" +
                    "PASSW|PASSWORD|" +
                    "PAU|PAUSE|" +
                    "PRINT|" +
                    "QUIT|" +
                    "RECOVER|" +
                    "REPF|REPFOOTER|" +
                    "REPH|REPHEADER|" +
                    "R|RUN|" +
                    "SAV|SAVE|" +
                    "SET|" +
                    "SHO|SHOW|" +
                    "SHUTDOWN|" +
                    "SPO|SPOOL|" +
                    "STA|START|" +
                    "STARTUP|" +
                    "STORE|" +
                    "TIMI|TIMING|" +
                    "TTI|TTITLE|" +
                    "UNDEF|UNDEFINE|" +
                    "VAR|VARIABLE|" +
                    "WHENEVER|" +
                    "XQUERY";
    private static final Pattern UNSUPPORTED_SQLPLUS_COMMANDS_REGEX = toRegex(UNSUPPORTED_SQLPLUS_COMMANDS);

    private static final String EXECUTE_COMMANDS = "EXEC|EXECUTE";
    private static final Pattern EXECUTE_REGEX = toRegex(EXECUTE_COMMANDS);
    private static final String PROMPT_COMMANDS = "PRO|PROMPT";
    private static final Pattern PROMPT_REGEX = toRegex(PROMPT_COMMANDS);
    private static final String REMARK_COMMANDS = "REM|REMARK";
    private static final Pattern REMARK_REGEX = toRegex(REMARK_COMMANDS);
    private static final Pattern SQLPLUS_REGEX =
            toRegex(UNSUPPORTED_SQLPLUS_COMMANDS, EXECUTE_COMMANDS, PROMPT_COMMANDS, REMARK_COMMANDS);
    // [/pro]

    private static final Pattern DECLARE_BEGIN_REGEX = Pattern.compile("(DECLARE|BEGIN)(\\s.*)?");
    private static final Pattern PLSQL_REGEX = Pattern.compile(
            "CREATE(\\s+OR\\s+REPLACE)?(\\s+(NON)?EDITIONABLE)?\\s+(FUNCTION|PROCEDURE|PACKAGE|TYPE|TRIGGER).*");
    private static final Pattern JAVA_REGEX = Pattern.compile(
            "CREATE(\\s+OR\\s+REPLACE)?(\\s+AND\\s+(RESOLVE|COMPILE))?(\\s+NOFORCE)?\\s+JAVA\\s+(SOURCE|RESOURCE|CLASS).*");

    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    private static final Delimiter PLSQL_DELIMITER = new Delimiter("/", true);

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    public OracleSqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    // [pro]
    @Override
    public SqlStatement getSqlStatement() {
        if (EXECUTE_REGEX.matcher(statementStart).matches()) {
            return new org.flywaydb.core.internal.database.oracle.pro.SQLPlusExecuteSqlStatement(lineNumber, statement.toString());
        }
        if (PROMPT_REGEX.matcher(statementStart).matches()) {
            return new org.flywaydb.core.internal.database.oracle.pro.SQLPlusPromptSqlStatement(lineNumber, statement.toString());
        }
        if (REMARK_REGEX.matcher(statementStart).matches()) {
            return new org.flywaydb.core.internal.database.oracle.pro.SQLPlusRemarkSqlStatement(lineNumber, statement.toString());
        }
        return super.getSqlStatement();
    }

    @Override
    public boolean isTerminated() {
        return (SQLPLUS_REGEX.matcher(statementStart).matches() && !statement.toString().endsWith("-"))
                || super.isTerminated();
    }
    // [/pro]

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (DECLARE_BEGIN_REGEX.matcher(line).matches()) {
            return PLSQL_DELIMITER;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 8) {
            statementStart += line;
            statementStart += " ";
            statementStart = statementStart.replaceAll("\\s+", " ");
        }

        if (PLSQL_REGEX.matcher(statementStart).matches() || JAVA_REGEX.matcher(statementStart).matches()) {
            return PLSQL_DELIMITER;
        }

        return delimiter;
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("'") && token.endsWith("'")) {
            return token;
        }

        Matcher beforeMatcher = KEYWORDS_BEFORE_STRING_LITERAL_REGEX.matcher(token);
        if (beforeMatcher.find()) {
            token = beforeMatcher.group(2);
        }

        Matcher afterMatcher = KEYWORDS_AFTER_STRING_LITERAL_REGEX.matcher(token);
        if (afterMatcher.find()) {
            token = afterMatcher.group(1);
        }

        return token;
    }

    @Override
    protected String simplifyLine(String line) {
        String simplifiedQQuotes = StringUtils.replaceAll(StringUtils.replaceAll(line, "q'(", "q'["), ")'", "]'");
        return super.simplifyLine(simplifiedQQuotes);
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("Q'") && (token.length() >= 3)) {
            return token.substring(0, 3);
        }
        return null;
    }

    @Override
    protected String computeAlternateCloseQuote(String openQuote) {
        char specialChar = openQuote.charAt(2);
        switch (specialChar) {
            case '[':
                return "]'";
            case '(':
                return ")'";
            case '{':
                return "}'";
            case '<':
                return ">'";
            default:
                return specialChar + "'";
        }
    }

    @Override
    public boolean canDiscard() {
        return super.canDiscard()
                || statementStart.startsWith("/") // Lone / that can safely be ignored
                // [pro]
                || isUnsupportedSqlPlusStatement()
                // [/pro]
                ;
    }

    // [pro]
    boolean isUnsupportedSqlPlusStatement() {
        if (statementStart.startsWith("@")
                || UNSUPPORTED_SQLPLUS_COMMANDS_REGEX.matcher(statementStart).matches()) {
            LOG.warn("Ignoring unsupported SQL*Plus statement: " + statement.toString());
            return true;
        }
        return false;
    }
    // [/pro]
}
