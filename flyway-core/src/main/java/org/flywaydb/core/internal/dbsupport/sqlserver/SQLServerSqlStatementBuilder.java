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
package org.flywaydb.core.internal.dbsupport.sqlserver;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting SQL Server-specific delimiter changes.
 */
public class SQLServerSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Regex for keywords that can appear before a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX = Pattern.compile("^(LIKE)('.*)");

    @Override
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter("GO", true);
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

        return token;
    }
}
