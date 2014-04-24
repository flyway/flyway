/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.dbsupport.redshift;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.dbsupport.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting PostgreSQL specific syntax.
 */
public class RedshiftSqlStatementBuilder extends SqlStatementBuilder
{
    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    /*private -> for testing*/static final String DOLLAR_QUOTE_REGEX = "\\$[A-Za-z0-9_]*\\$.*";

    @Override
    protected String extractAlternateOpenQuote(String token)
    {
        Matcher matcher = Pattern.compile(DOLLAR_QUOTE_REGEX).matcher(token);
        if (matcher.find()) {
            return token.substring(matcher.start(), matcher.end());
        }
        return null;
    }
}