/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.configuration.Configuration;

import org.flywaydb.core.internal.parser.*;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.regex.Pattern;

public class OracleParser extends Parser {





    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    private static final Delimiter PLSQL_DELIMITER = new Delimiter("/", true



    );





    private static final Pattern PLSQL_VIEW_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\sVIEW\\s.*\\sAS\\sWITH\\s(PROCEDURE|FUNCTION)");
    private static final StatementType PLSQL_VIEW_STATEMENT = new StatementType();

    private static final Pattern PLSQL_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\s(FUNCTION|PROCEDURE|PACKAGE|TYPE|TRIGGER)");
    private static final Pattern JAVA_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\sAND\\s(RESOLVE|COMPILE))?(\\sNOFORCE)?\\sJAVA\\s(SOURCE|RESOURCE|CLASS)");
    private static final Pattern DECLARE_BEGIN_REGEX = Pattern.compile("^DECLARE|BEGIN|WITH");
    private static final StatementType PLSQL_STATEMENT = new StatementType();

    private static Pattern toRegex(String... commands) {
        return Pattern.compile(toRegexPattern(commands));
    }

    private static String toRegexPattern(String... commands) {
        return "^(" + StringUtils.arrayToDelimitedString("|", commands) + ")";
    }



































































































    public OracleParser(Configuration configuration






            , ParsingContext parsingContext
    ) {
        super(configuration, parsingContext, 3);






    }


















    @Override
    protected ParsedSqlStatement createStatement(PeekingReader reader, Recorder recorder,
                                                 int statementPos, int statementLine, int statementCol,
                                                 int nonCommentPartPos, int nonCommentPartLine, int nonCommentPartCol,
                                                 StatementType statementType, boolean canExecuteInTransaction,
                                                 Delimiter delimiter, String sql



    ) throws IOException {















































        if (PLSQL_VIEW_STATEMENT == statementType) {
            sql = sql.trim();

            // Strip extra semicolon to avoid issues with WITH statements containing PL/SQL
            if (sql.endsWith(";")) {
                sql = sql.substring(0, sql.length() - 1);
            }
        }

        return super.createStatement(reader, recorder, statementPos, statementLine, statementCol,
                nonCommentPartPos, nonCommentPartLine, nonCommentPartCol,
                statementType, canExecuteInTransaction, delimiter, sql



        );
    }

    @Override
    protected StatementType detectStatementType(String simplifiedStatement) {
        if (PLSQL_REGEX.matcher(simplifiedStatement).matches()
                || DECLARE_BEGIN_REGEX.matcher(simplifiedStatement).matches()
                || JAVA_REGEX.matcher(simplifiedStatement).matches()) {
            return PLSQL_STATEMENT;
        }

        if (PLSQL_VIEW_REGEX.matcher(simplifiedStatement).matches()) {
            return PLSQL_VIEW_STATEMENT;
        }




































        return super.detectStatementType(simplifiedStatement);
    }

    @Override
    protected boolean shouldDiscard(Token token, boolean nonCommentPartSeen) {
        // Discard dangling PL/SQL / delimiters
        return ("/".equals(token.getText()) && !nonCommentPartSeen) || super.shouldDiscard(token, nonCommentPartSeen);
    }

    @Override
    protected void adjustDelimiter(ParserContext context, StatementType statementType) {
        if (statementType == PLSQL_STATEMENT || statementType == PLSQL_VIEW_STATEMENT) {
            context.setDelimiter(PLSQL_DELIMITER);




        } else {
            context.setDelimiter(Delimiter.SEMICOLON);
        }
    }














    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword) {
        String keywordText = keyword.getText();
        int parensDepth = keyword.getParensDepth();

        if ("BEGIN".equals(keywordText) || (("IF".equals(keywordText)
                || "CASE".equals(keywordText) || "LOOP".equals(keywordText)) && !containsWithinLast(1, tokens, parensDepth, "END"))
                || ("TRIGGER".equals(keywordText) && containsWithinLast(1, tokens, parensDepth, "COMPOUND"))
                || (("AS".equals(keywordText) || "IS".equals(keywordText)) && (
                containsWithinLast(4, tokens, parensDepth, "PACKAGE")
                        || containsWithinLast(3, tokens, parensDepth, "PACKAGE", "BODY")
                        || containsWithinLast(3, tokens, parensDepth, "TYPE", "BODY")))
        ) {
            context.increaseBlockDepth();
        } else if ("END".equals(keywordText)) {
            context.decreaseBlockDepth();
        }
    }

    /**
     * Checks if the specified token texts are found consecutively within the last tokens in the list
     */
    private static boolean containsWithinLast(int count, List<Token> tokens, int parensDepth, String... consecutiveTokenTexts) {
        int j = consecutiveTokenTexts.length - 1;
        int remaining = count;
        for (int i = tokens.size() - 1; i >= 0 && remaining > 0; i--) {
            // Only consider tokens at the same parenthesis depth
            if (tokens.get(i).getParensDepth() != parensDepth) {
                continue;
            }
            if (consecutiveTokenTexts[j].equals(tokens.get(i).getText())) {
                if (j == 0) {
                    return true;
                }
                j--;
            } else if (j < consecutiveTokenTexts.length - 1) {
                // Token texts weren't consecutive, so reset
                j = consecutiveTokenTexts.length - 1;
            }
            remaining--;
        }
        return false;
    }

    @Override
    protected boolean isDelimiter(String peek, ParserContext context, int col) {
        Delimiter delimiter = context.getDelimiter();

        // Only consider alone-on-line delimiters (such as "/" for PL/SQL) if
        // we're at the top level and it's the first character on the line
        if (delimiter.isAloneOnLine() && (context.getBlockDepth() > 0 || col > 1)) {
            return false;
        }







        return super.isDelimiter(peek, context, col);
    }














    @Override
    protected boolean isAlternativeStringLiteral(String peek) {
        if (peek.length() < 3) {
            return false;
        }

        return peek.charAt(0) == 'q' && peek.charAt(1) == '\'';
    }

    @Override
    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow(2);
        String closeQuote = computeAlternativeCloseQuote((char) reader.read());
        reader.swallowUntilExcluding(closeQuote);
        reader.swallow(closeQuote.length());
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    private String computeAlternativeCloseQuote(char specialChar) {
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