/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.dbsupport.Delimiter;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

/**
 * SqlStatementBuilder supporting Oracle-specific PL/SQL constructs.
 */
public class OracleSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    private static final Delimiter PLSQL_DELIMITER = new Delimiter("/", true);

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.matches("DECLARE|DECLARE\\s.*") || line.matches("BEGIN|BEGIN\\s.*")) {
            return PLSQL_DELIMITER;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (statementStart.startsWith("CREATE FUNCTION")
                || statementStart.startsWith("CREATE PROCEDURE")
                || statementStart.startsWith("CREATE PACKAGE")
                || statementStart.startsWith("CREATE TYPE")
                || statementStart.startsWith("CREATE TRIGGER")
                || statementStart.startsWith("CREATE OR REPLACE FUNCTION")
                || statementStart.startsWith("CREATE OR REPLACE PROCEDURE")
                || statementStart.startsWith("CREATE OR REPLACE PACKAGE")
                || statementStart.startsWith("CREATE OR REPLACE TYPE")
                || statementStart.startsWith("CREATE OR REPLACE TRIGGER")){
            return PLSQL_DELIMITER;
        }

        return delimiter;
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
}
