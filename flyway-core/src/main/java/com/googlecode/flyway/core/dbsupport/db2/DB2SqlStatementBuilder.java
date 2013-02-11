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
package com.googlecode.flyway.core.dbsupport.db2;

import com.googlecode.flyway.core.dbsupport.Delimiter;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlStatementBuilder supporting DB2-specific delimiter changes.
 */
public class DB2SqlStatementBuilder extends SqlStatementBuilder {
    /**
     * Are we currently inside a BEGIN END; block?
     */
    private boolean insideBeginEndBlock;

    /**
     * Are we inside a ' multi-line string literal
     */
    private boolean insideQuoteStringLiteral = false;

    /**
     * Are we inside a multi-line /*  *&#47; comment.
     */
    private boolean insideMultiLineComment = false;

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (statementStart.startsWith("CREATE FUNCTION")
                || statementStart.startsWith("CREATE PROCEDURE")
                || statementStart.startsWith("CREATE TRIGGER")
                || statementStart.startsWith("CREATE OR REPLACE FUNCTION")
                || statementStart.startsWith("CREATE OR REPLACE PROCEDURE")
                || statementStart.startsWith("CREATE OR REPLACE TRIGGER")) {
            if (line.startsWith("BEGIN")) {
                insideBeginEndBlock = true;
            }

            if (line.endsWith("END;")) {
                insideBeginEndBlock = false;
            }
        }

        if (insideBeginEndBlock) {
            return null;
        }
        return getDefaultDelimiter();
    }

    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String line) {
        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(line, " ;=|(),");

        List<TokenType> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        for (TokenType delimitingToken : delimitingTokens) {
            if (!insideQuoteStringLiteral && (delimitingToken == TokenType.MULTI_LINE_COMMENT_OPEN)) {
                insideMultiLineComment = true;
            }

            if (insideMultiLineComment && TokenType.MULTI_LINE_COMMENT_CLOSE.equals(delimitingToken)) {
                insideMultiLineComment = false;
            }

            if (!insideQuoteStringLiteral && !insideMultiLineComment && (delimitingToken == TokenType.SINGLE_LINE_COMMENT)) {
                return false;
            }

            if (!insideMultiLineComment && (delimitingToken == TokenType.QUOTE)) {
                insideQuoteStringLiteral = !insideQuoteStringLiteral;
            }
        }

        return insideQuoteStringLiteral;
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     *         impact on string delimiting are discarded.
     */
    private List<TokenType> extractStringLiteralDelimitingTokens(String[] tokens) {

        List<TokenType> delimitingTokens = new ArrayList<TokenType>();
        for (String token : tokens) {
            if ((token.length() > 1) && token.startsWith("'") && token.endsWith("'")) {
                //Skip '', 'abc', ...
                continue;
            }

            if (token.startsWith("--")) {
                delimitingTokens.add(TokenType.SINGLE_LINE_COMMENT);
            } else if (token.startsWith("'") || token.endsWith("'")) {
                delimitingTokens.add(TokenType.QUOTE);
            } else if (token.startsWith("--")) {
                delimitingTokens.add(TokenType.SINGLE_LINE_COMMENT);
            } else if (token.startsWith("/*")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_OPEN);
            } else if (token.startsWith("*/")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_CLOSE);
            }
        }

        return delimitingTokens;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private static enum TokenType {
        /**
         * Token opens or closes ' string literal
         */
        QUOTE,

        /**
         * Token starts end of line comment --
         */
        SINGLE_LINE_COMMENT,

        /**
         * Token opens multi-line comment /*
         */
        MULTI_LINE_COMMENT_OPEN,

        /**
         * Token closes multi-line comment *&#47;
         */
        MULTI_LINE_COMMENT_CLOSE
    }
}
