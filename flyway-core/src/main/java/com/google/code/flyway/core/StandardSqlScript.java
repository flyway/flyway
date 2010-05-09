/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core;

import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Standard sql script, without any proprietary sql vendor extensions.
 */
public class StandardSqlScript extends SqlScript {
    /**
     * The default Statement delimiter.
     */
    private static final String DEFAULT_STATEMENT_DELIMITER = ";";

    /**
     * Creates a new sql script from this resource with these placeholders to replace.
     *
     * @param resource     The resource containing the sql script.
     * @param placeholders A map of <placeholder, replacementValue> to replace in sql statements.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public StandardSqlScript(Resource resource, Map<String, String> placeholders) {
        super(resource, placeholders);
    }

    /**
     * Dummy constructor to increase testability.
     */
    public StandardSqlScript() {
        super();
    }

    /**
     * Turns these lines in a series of statements.
     *
     * @param lines The lines to analyse.
     *
     * @return The statements contained in these lines (in order).
     */
    protected List<SqlStatement> linesToStatements(List<String> lines) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        int statementLineNumber = 0;
        String statementSql = "";

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);

            if (line.isEmpty()) {
                continue;
            }

            if (statementSql.isEmpty()) {
                statementLineNumber = lineNumber;
            } else {
                statementSql += " ";
            }
            statementSql += line;

            if (line.endsWith(DEFAULT_STATEMENT_DELIMITER)) {
                int humanAdjustedStatementLineNumber = statementLineNumber + 1;
                String noDelimiterStatementSql = stripDelimiter(statementSql, DEFAULT_STATEMENT_DELIMITER);
                statements.add(new SqlStatement(humanAdjustedStatementLineNumber, noDelimiterStatementSql));

                statementLineNumber = lineNumber + 1;
                statementSql = "";
            }
        }

        // Catch any statements not followed by delimiter.
        if (!statementSql.isEmpty()) {
            statements.add(new SqlStatement(statementLineNumber, statementSql));
        }

        return statements;
    }

    /**
     * Strips this delimiter from this sql statement.
     *
     * @param sql       The statement to parse.
     * @param delimiter The delimiter to strip.
     * @return The sql statement without delimiter.
     */
    private static String stripDelimiter(String sql, String delimiter) {
        return sql.substring(0, sql.length() - delimiter.length());
    }
}
