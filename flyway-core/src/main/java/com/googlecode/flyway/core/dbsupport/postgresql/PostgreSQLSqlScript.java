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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlScript supporting PostgreSQL routine definitions.
 */
public class PostgreSQLSqlScript extends SqlScript {

    public static final Pattern DOLLAR_QUOTE_PATTERN = Pattern.compile(".*? AS (\\$[A-Z0-9_]*\\$).*");

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     *
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public PostgreSQLSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        super(sqlScriptSource, placeholderReplacer);
    }

    @Override
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter) {
        String upperCaseStatement = statement.toUpperCase();

        if (upperCaseStatement.startsWith("CREATE") && upperCaseStatement.contains("FUNCTION")) {
            Matcher matcher = DOLLAR_QUOTE_PATTERN.matcher(upperCaseStatement);
            if (matcher.matches()) {
                String dollarTagName = Pattern.quote(matcher.group(1));
                if (upperCaseStatement.matches(".*(" + dollarTagName + ").*\\1.*")) {
                    return ";";
                } else {
                    return null;
                }
            }
        }

        return delimiter;
    }

    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        int numQuotes = StringUtils.countOccurrencesOf(statement, "'");
        return (numQuotes % 2) != 0;
    }
}
