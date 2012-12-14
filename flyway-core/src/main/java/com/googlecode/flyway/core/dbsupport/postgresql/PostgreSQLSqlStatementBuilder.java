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

import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SqlStatementBuilder supporting PostgreSQL specific syntax.
 */
public class PostgreSQLSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    /*private -> for testing*/
    static final String DOLLAR_QUOTE_REGEX = "\\$[A-Za-z0-9_]*\\$.*";

    /**
     * Are we currently in a ' multi-line string literal
     */
    private boolean insideQuoteStringLiteral = false;

    /**
     * Are we currently in a $$ multi-line string literal
     */
    private boolean insideDollarStringLiteral = false;

    /**
     * The current dollar closing quote to look for.
     */
    private String dollarQuote = null;

    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String line) {
        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(line, " ;=|(),");

        List<Set<TokenType>> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        for (Set<TokenType> delimitingToken : delimitingTokens) {
            if (!insideDollarStringLiteral && !insideQuoteStringLiteral && delimitingToken.contains(TokenType.QUOTE_OPEN)) {
                insideQuoteStringLiteral = true;
                continue;
            }
            if (insideQuoteStringLiteral && delimitingToken.contains(TokenType.QUOTE_CLOSE)) {
                insideQuoteStringLiteral = false;
                continue;
            }
            if (!insideDollarStringLiteral && !insideQuoteStringLiteral && delimitingToken.contains(TokenType.DOLLAR_OPEN)) {
                insideDollarStringLiteral = true;
                continue;
            }
            if (insideDollarStringLiteral && delimitingToken.contains(TokenType.DOLLAR_CLOSE)) {
                insideDollarStringLiteral = false;
            }
        }

        return insideQuoteStringLiteral || insideDollarStringLiteral;
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     *         impact on string delimiting are discarded.
     */
    private List<Set<TokenType>> extractStringLiteralDelimitingTokens(String[] tokens) {
        List<Set<TokenType>> delimitingTokens = new ArrayList<Set<TokenType>>();
        for (String token : tokens) {
            //Remove escaped quotes as they do not form a string literal delimiter
            String cleanToken = StringUtils.replace(token, "''", "");

            Set<TokenType> tokenTypes = new HashSet<TokenType>();

            if (cleanToken.startsWith("'")) {
                if ((cleanToken.length() > 1) && cleanToken.endsWith("'")) {
                    // Ignore. ' string literal is opened and closed inside the same token.
                    continue;
                }
                tokenTypes.add(TokenType.QUOTE_OPEN);
            }

            if (cleanToken.endsWith("'")) {
                tokenTypes.add(TokenType.QUOTE_CLOSE);
            }

            if ((dollarQuote == null) && cleanToken.matches(DOLLAR_QUOTE_REGEX)) {
                dollarQuote = cleanToken.substring(0, cleanToken.substring(1).indexOf("$") + 2);
                if ((cleanToken.length() > dollarQuote.length()) && cleanToken.endsWith(dollarQuote)) {
                    // Ignore. $$ string literal is opened and closed inside the same token.
                    dollarQuote = null;
                    continue;
                }
                tokenTypes.add(TokenType.DOLLAR_OPEN);
            }

            if ((dollarQuote != null) && !cleanToken.startsWith(dollarQuote) && cleanToken.endsWith(dollarQuote)) {
                tokenTypes.add(TokenType.DOLLAR_CLOSE);
                dollarQuote = null;
            }

            if (!tokenTypes.isEmpty()) {
                delimitingTokens.add(tokenTypes);
            }
        }

        return delimitingTokens;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private static enum TokenType {
        /**
         * Token opens ' string literal
         */
        QUOTE_OPEN,

        /**
         * Token closes ' string literal
         */
        QUOTE_CLOSE,

        /**
         * Token opens $$ string literal
         */
        DOLLAR_OPEN,

        /**
         * Token closes $$ string literal
         */
        DOLLAR_CLOSE
    }
}
