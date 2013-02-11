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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.dbsupport.Delimiter;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlStatementBuilder supporting MySQL-specific delimiter changes.
 */
public class MySQLSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "DELIMITER";

    /**
     * Are we currently inside a ' multi-line string literal.
     */
    private boolean insideQuoteStringLiteral = false;

    /**
     * Are we currently inside a " multi-line string literal.
     */
    private boolean insideDoubleQuoteStringLiteral = false;

    /**
     * Are we inside a multi-line /*  *&#47; comment.
     */
    private boolean insideMultiLineComment = false;

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return null;
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return delimiter;
    }

    @Override
    public boolean isCommentDirective(String line) {
        return line.startsWith("/*!") && line.endsWith("*/;");
    }

    @Override
    public boolean isSingleLineComment(String line) {
        return super.isSingleLineComment(line) || line.startsWith("#");
    }

    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String line) {
        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(line, " ;=|(),");

        List<Token> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        for (Token delimitingToken : delimitingTokens) {
            boolean moreTokenTypesApplicable = true;
            for (TokenType tokenType : delimitingToken.tokenTypes) {
                if (!moreTokenTypesApplicable) {
                    continue;
                }

                if (!insideQuoteStringLiteral && !insideDoubleQuoteStringLiteral && (tokenType == TokenType.MULTI_LINE_COMMENT_OPEN)) {
                    insideMultiLineComment = true;
                    moreTokenTypesApplicable = false;
                }

                if (insideMultiLineComment && TokenType.MULTI_LINE_COMMENT_CLOSE.equals(tokenType)) {
                    insideMultiLineComment = false;
                    moreTokenTypesApplicable = false;
                }

                if (!insideQuoteStringLiteral && !insideDoubleQuoteStringLiteral && (tokenType == TokenType.SINGLE_LINE_COMMENT)) {
                    return false;
                }

                if (!insideMultiLineComment && !insideDoubleQuoteStringLiteral && !insideQuoteStringLiteral && (tokenType == TokenType.SINGLE_OPEN)) {
                    insideQuoteStringLiteral = true;
                    if (delimitingToken.singleTypeApplicable) {
                        moreTokenTypesApplicable = false;
                    }
                    continue;
                }
                if (insideQuoteStringLiteral && (tokenType == TokenType.SINGLE_CLOSE)) {
                    insideQuoteStringLiteral = false;
                    moreTokenTypesApplicable = false;
                    continue;
                }
                if (!insideMultiLineComment && !insideDoubleQuoteStringLiteral && !insideQuoteStringLiteral && (tokenType == TokenType.DOUBLE_OPEN)) {
                    insideDoubleQuoteStringLiteral = true;
                    continue;
                }
                if (insideDoubleQuoteStringLiteral && (tokenType == TokenType.DOUBLE_CLOSE)) {
                    insideDoubleQuoteStringLiteral = false;
                    moreTokenTypesApplicable = false;
                }
            }
        }

        return insideQuoteStringLiteral || insideDoubleQuoteStringLiteral;
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
            String noQuoteQuote = StringUtils.replace(token, "''", "");
            String cleanToken = StringUtils.replace(noQuoteQuote, "\\'", "");

            List<TokenType> tokenTypes = new ArrayList<TokenType>();

            if (cleanToken.startsWith("--")) {
                tokenTypes.add(TokenType.SINGLE_LINE_COMMENT);
            }

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

            if (cleanToken.startsWith("/*")) {
                tokenTypes.add(TokenType.MULTI_LINE_COMMENT_OPEN);
            }

            if (cleanToken.startsWith("*/")) {
                tokenTypes.add(TokenType.MULTI_LINE_COMMENT_CLOSE);
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
        DOUBLE_CLOSE,

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
