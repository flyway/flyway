/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.postgresql;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting PostgreSQL specific syntax.
 */
public class PostgreSQLSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Delimiter of COPY statements.
     */
    private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true);

    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    /*private -> for testing*/
    static final String DOLLAR_QUOTE_REGEX = "(\\$[A-Za-z0-9_]*\\$).*";

    /**
     * Are we at the beginning of the statement.
     */
    private boolean firstLine = true;

    /**
     * The copy statement seen so far.
     */
    private String copyStatement;

    /**
     * Whether this statement is a COPY statement.
     */
    private boolean pgCopy;

    @Override
    protected String extractAlternateOpenQuote(String token) {
        Matcher matcher = Pattern.compile(DOLLAR_QUOTE_REGEX).matcher(token);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (pgCopy) {
            return COPY_DELIMITER;
        }

        if (firstLine) {
            firstLine = false;
            if (line.matches("COPY|COPY\\s.*")) {
                copyStatement = line;
            }
        } else if (copyStatement != null) {
            copyStatement += " " + line;
        }

        if (copyStatement != null && copyStatement.contains(" FROM STDIN")) {
            pgCopy = true;
            return COPY_DELIMITER;
        }

        return delimiter;
    }

    @Override
    public boolean isPgCopyFromStdIn() {
        return pgCopy;
    }
}
