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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting SQL Server-specific delimiter changes.
 */
public class SQLServerSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Regex for keywords that can appear before a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX = Pattern.compile("^(LIKE|AS)('.*)");

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(LIKE|AS)$");

    /**
     * Regex for statements that cannot be executed within a transaction.
     */
    private static final Pattern NON_TRANSACTIONAL_STATEMENT_REGEX =
            Pattern.compile("^((BACKUP|RESTORE|RECONFIGURE|(CREATE|DROP|ALTER) (DATABASE|FULLTEXT INDEX))|" +
                    // #2175: The procedure 'sp_addsubscription' cannot be executed within a transaction.
                    // This procedure is only present in SQL Server. Not on Azure nor in PDW.
                    "(EXEC SP_ADDSUBSCRIPTION)" +
                    ");?( .*)?");

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    public SQLServerSqlStatementBuilder() {
        super(Delimiter.GO);
    }

    @Override
    protected void applyStateChanges(String line) {
        super.applyStateChanges(line);

        if (!executeInTransaction || !hasNonCommentPart()) {
            return;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 3) {
            statementStart += line;
            statementStart += " ";
            statementStart = StringUtils.collapseWhitespace(statementStart);
        }

        if (NON_TRANSACTIONAL_STATEMENT_REGEX.matcher(statementStart).matches() ||
                // Handle statements inside nested blocks
                (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral && !insideMultiLineComment
                        && NON_TRANSACTIONAL_STATEMENT_REGEX.matcher(line).matches())) {
            executeInTransaction = false;
        }
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("N'")) {
            return token.substring(token.indexOf("'"));
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
}