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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.migration.sql.Delimiter;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlScript supporting MySQL-specific delimiter changes.
 */
public class MySQLSqlScript extends SqlScript {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "DELIMITER";

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public MySQLSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        super(sqlScriptSource, placeholderReplacer);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(StringBuilder statement, String line, Delimiter delimiter) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return delimiter;
    }

    @Override
    protected boolean isDelimiterChangeExplicit() {
        return true;
    }

    @Override
    protected boolean isCommentDirective(String line) {
        return line.startsWith("/*!") && line.endsWith("*/;");
    }

    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(statement, " ;=|(),");

        List<Token> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        boolean insideQuoteStringLiteral = false;
        boolean insideQStringLiteral = false;

        for (Token delimitingToken : delimitingTokens) {
            boolean moreTokensApplicable = true;
            for (TokenType tokenType : delimitingToken.tokenTypes) {
                if (!moreTokensApplicable) {
                    continue;
                }

                if (!insideQStringLiteral && !insideQuoteStringLiteral && (tokenType == TokenType.SINGLE_OPEN)) {
                    insideQuoteStringLiteral = true;
                    if (delimitingToken.singleTypeApplicable) {
                        moreTokensApplicable = false;
                    }
                    continue;
                }
                if (insideQuoteStringLiteral && (tokenType == TokenType.SINGLE_CLOSE)) {
                    insideQuoteStringLiteral = false;
                    moreTokensApplicable = false;
                    continue;
                }
                if (!insideQStringLiteral && !insideQuoteStringLiteral && (tokenType == TokenType.DOUBLE_OPEN)) {
                    insideQStringLiteral = true;
                    continue;
                }
                if (insideQStringLiteral && (tokenType == TokenType.DOUBLE_CLOSE)) {
                    insideQStringLiteral = false;
                    moreTokensApplicable = false;
                }
            }
        }

        return insideQuoteStringLiteral || insideQStringLiteral;
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     *         impact on string delimiting are discarded.
     */
    private List<Token> extractStringLiteralDelimitingTokens(String[] tokens) {
        List<Token> delimitingTokens = new ArrayList<Token>();
        for (String token : tokens) {
            //Remove escaped quotes as they do not form a string literal delimiter
            String cleanToken = StringUtils.replace(token, "''", "");

            List<TokenType> tokenTypes = new ArrayList<TokenType>();

            if (cleanToken.startsWith("'")) {
                tokenTypes.add(TokenType.SINGLE_OPEN);
            }

            if (cleanToken.endsWith("'")) {
                tokenTypes.add(TokenType.SINGLE_CLOSE);
            }

            if (cleanToken.startsWith("\"")) {
                tokenTypes.add(TokenType.DOUBLE_OPEN);
            }

            if (cleanToken.endsWith("\"")) {
                tokenTypes.add(TokenType.DOUBLE_CLOSE);
            }

            if (!tokenTypes.isEmpty()) {
                Token parsedToken = new Token();
                parsedToken.tokenTypes = tokenTypes;
                parsedToken.singleTypeApplicable = token.length() == 1;
                delimitingTokens.add(parsedToken);
            }
        }

        return delimitingTokens;
    }

    /**
     * A parsed token.
     */
    private class Token {
        /**
         * TokenTypes of this token.
         */
        public List<TokenType> tokenTypes;

        /**
         * Flag indicating whether only a single tokenType may influence the parsing.
         */
        public boolean singleTypeApplicable;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private static enum TokenType {
        /**
         * Token opens ' string literal
         */
        SINGLE_OPEN,

        /**
         * Token closes ' string literal
         */
        SINGLE_CLOSE,

        /**
         * Token opens " string literal
         */
        DOUBLE_OPEN,

        /**
         * Token closes " string literal
         */
        DOUBLE_CLOSE
    }
}
