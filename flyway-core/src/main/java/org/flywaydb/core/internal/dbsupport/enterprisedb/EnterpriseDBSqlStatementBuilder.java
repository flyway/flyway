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
package org.flywaydb.core.internal.dbsupport.enterprisedb;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting Oracle-specific PL/SQL constructs.
 */
public class EnterpriseDBSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Regex for keywords that can appear before a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX = Pattern.compile("^(N|IF|ELSIF|SELECT|IMMEDIATE|RETURN|IS)('.*)");

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(USING|THEN|FROM|AND|OR)(?!.)");

    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    /*private -> for testing*/
    static final String DOLLAR_QUOTE_REGEX = "(\\$[A-Za-z0-9_]*\\$).*";

    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    private static final Delimiter SPL_DELIMITER = new Delimiter("/", true);

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.matches("DECLARE|DECLARE\\s.*") || line.matches("BEGIN|BEGIN\\s.*")) {
            return SPL_DELIMITER;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 8) {
            statementStart += line;
            statementStart += " ";
            statementStart = statementStart.replaceAll("\\s+", " ");
        }

        if (statementStart.matches("CREATE( OR REPLACE)? (FUNCTION|PROCEDURE|PACKAGE|TYPE|TRIGGER).*")) {
            return SPL_DELIMITER;
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
        Matcher matcher = Pattern.compile(DOLLAR_QUOTE_REGEX).matcher(token);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public boolean canDiscard() {
        return super.canDiscard() || statementStart.startsWith("SET DEFINE OFF");
    }
}
