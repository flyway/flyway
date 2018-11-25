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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
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
    private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX =
            Pattern.compile("^(N|DATE|IF|ELSIF|SELECT|IMMEDIATE|RETURN|IS)('.*)");

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(USING|THEN|FROM|AND|OR|AS)(?!.)");

    private static Pattern toRegex(String... commands) {
        return Pattern.compile(toRegexPattern(commands));
    }

    private static String toRegexPattern(String... commands) {
        return "^(" + StringUtils.arrayToDelimitedString("|", commands) + ")(\\s.*)?";
    }





















































































    private static final Pattern DECLARE_BEGIN_REGEX = toRegex("DECLARE|BEGIN");
    private static final Pattern PLSQL_VIEW_REGEX = Pattern.compile(
            "^CREATE(\\s+OR\\s+REPLACE)?(\\s+(NON)?EDITIONABLE)?\\s+(VIEW)\\s+.*\\s+AS\\s+WITH.*");
    private static final Pattern PLSQL_REGEX = Pattern.compile(
            "^CREATE(\\s+OR\\s+REPLACE)?(\\s+(NON)?EDITIONABLE)?\\s+(FUNCTION|PROCEDURE|PACKAGE|TYPE|TRIGGER).*");
    private static final Pattern JAVA_REGEX = Pattern.compile(
            "^CREATE(\\s+OR\\s+REPLACE)?(\\s+AND\\s+(RESOLVE|COMPILE))?(\\s+NOFORCE)?\\s+JAVA\\s+(SOURCE|RESOURCE|CLASS).*");

    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    static final Delimiter PLSQL_DELIMITER = new Delimiter("/", true);








    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    public OracleSqlStatementBuilder(






    ) {
        super(Delimiter.SEMICOLON);






    }




















































    @Override
    protected void applyStateChanges(String line) {
        super.applyStateChanges(line);

        if (hasNonCommentPart() && StringUtils.countOccurrencesOf(statementStart, " ") < 8) {
            statementStart += line;
            statementStart += " ";
            statementStart = StringUtils.collapseWhitespace(statementStart);
        }
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (DECLARE_BEGIN_REGEX.matcher(line).matches()) {
            return PLSQL_DELIMITER;
        }

        if (PLSQL_REGEX.matcher(statementStart).matches()
                || JAVA_REGEX.matcher(statementStart).matches()
                || PLSQL_VIEW_REGEX.matcher(statementStart).matches()) {
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



                || statementStart.equals("/ "); // Lone / that can safely be ignored
    }










}