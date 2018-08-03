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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting CockroachDB specific syntax.
 */
public class CockroachDBSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    private static final String DOLLAR_QUOTE_REGEX = "(\\$[A-Za-z0-9_]*\\$).*";

    CockroachDBSqlStatementBuilder() {
        super(Delimiter.SEMICOLON);
    }

    @Override
    protected Collection<String> tokenizeLine(String line) {
        return StringUtils.tokenizeToStringCollection(line, " @<>;:=|(),+{}[]");
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
    protected String cleanToken(String token) {
        if (token.startsWith("E'")) {
            return token.substring(token.indexOf("'"));
        }

        return token;
    }
}