/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.parser.*;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.util.StringUtils;





import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class OracleParser extends Parser {












    /**
     * Delimiter of PL/SQL blocks and statements.
     */
    private static final Delimiter PLSQL_DELIMITER = new Delimiter("/", true



    );





    //                                                 accessible   by    (        keyword<space>optionalidentifier                      )
    private static final String ACCESSIBLE_BY_REGEX = "ACCESSIBLE\\sBY\\s\\(?((FUNCTION|PROCEDURE|PACKAGE|TRIGGER|TYPE)\\s[^\\s]*\\s?+)*\\)?";

    private static final Pattern PLSQL_TYPE_BODY_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\sTYPE\\sBODY\\s([^\\s]*\\s)?(IS|AS)");

    private static final Pattern PLSQL_PACKAGE_BODY_REGEX = Pattern.compile(
            "^CREATE(\\s*OR\\s*REPLACE)?(\\s*(NON)?EDITIONABLE)?\\s*PACKAGE\\s*BODY\\s*([^\\s]*\\s)?(IS|AS)");
    private static final StatementType PLSQL_PACKAGE_BODY_STATEMENT = new StatementType();

    private static final Pattern PLSQL_PACKAGE_DEFINITION_REGEX = Pattern.compile(
            "^CREATE(\\s*OR\\s*REPLACE)?(\\s*(NON)?EDITIONABLE)?\\s*PACKAGE\\s([^\\s*]*\\s*)?(AUTHID\\s*[^\\s*]*\\s*|" + ACCESSIBLE_BY_REGEX + ")*(IS|AS)");

    private static final Pattern PLSQL_VIEW_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\sVIEW\\s([^\\s]*\\s)?AS\\sWITH\\s(PROCEDURE|FUNCTION)");
    private static final StatementType PLSQL_VIEW_STATEMENT = new StatementType();

    private static final Pattern PLSQL_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\s(FUNCTION(\\s\\S*)|PROCEDURE|TYPE|TRIGGER)");
    private static final Pattern DECLARE_BEGIN_REGEX = Pattern.compile("^DECLARE|BEGIN|WITH");
    private static final StatementType PLSQL_STATEMENT = new StatementType();

    private static final Pattern JAVA_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\sAND\\s(RESOLVE|COMPILE))?(\\sNOFORCE)?\\sJAVA\\s(SOURCE|RESOURCE|CLASS)");
    private static final StatementType PLSQL_JAVA_STATEMENT = new StatementType();

    private static final Pattern PLSQL_PACKAGE_BODY_WRAPPED_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\sPACKAGE\\sBODY(\\s\\S*)?\\sWRAPPED(\\s\\S*)*");
    private static final Pattern PLSQL_PACKAGE_DEFINITION_WRAPPED_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\sPACKAGE(\\s\\S*)?\\sWRAPPED(\\s\\S*)*");
    private static final Pattern PLSQL_WRAPPED_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?(\\s(NON)?EDITIONABLE)?\\s(FUNCTION|PROCEDURE|TYPE)(\\s\\S*)?\\sWRAPPED(\\s\\S*)*");

    private static final StatementType PLSQL_WRAPPED_STATEMENT = new StatementType();
    private int initialWrappedBlockDepth = -1;

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
    protected StatementType detectStatementType(String simplifiedStatement, ParserContext context, PeekingReader reader) {
        if (PLSQL_PACKAGE_BODY_WRAPPED_REGEX.matcher(simplifiedStatement).matches()
                || PLSQL_PACKAGE_DEFINITION_WRAPPED_REGEX.matcher(simplifiedStatement).matches()
                || PLSQL_WRAPPED_REGEX.matcher(simplifiedStatement).matches()) {
            if (initialWrappedBlockDepth == -1) {
                initialWrappedBlockDepth = context.getBlockDepth();
            }
            return PLSQL_WRAPPED_STATEMENT;
        }

        if (PLSQL_PACKAGE_BODY_REGEX.matcher(simplifiedStatement).matches()) {
            return PLSQL_PACKAGE_BODY_STATEMENT;
        }

        if (PLSQL_REGEX.matcher(simplifiedStatement).matches()
                || PLSQL_PACKAGE_DEFINITION_REGEX.matcher(simplifiedStatement).matches()
                || DECLARE_BEGIN_REGEX.matcher(simplifiedStatement).matches()) {
            try {
                String wrappedKeyword = " WRAPPED";
                if (!reader.peek(wrappedKeyword.length()).equalsIgnoreCase(wrappedKeyword)) {
                    return PLSQL_STATEMENT;
                }
            } catch (IOException e) {
                return PLSQL_STATEMENT;
            }
        }

        if (JAVA_REGEX.matcher(simplifiedStatement).matches()) {
            return PLSQL_JAVA_STATEMENT;
        }

        if (PLSQL_VIEW_REGEX.matcher(simplifiedStatement).matches()) {
            return PLSQL_VIEW_STATEMENT;
        }







































        return super.detectStatementType(simplifiedStatement, context, reader);
    }

    @Override
    protected boolean shouldDiscard(Token token, boolean nonCommentPartSeen) {
        // Discard dangling PL/SQL '/' delimiters
        return ("/".equals(token.getText()) && !nonCommentPartSeen) || super.shouldDiscard(token, nonCommentPartSeen);
    }

    @Override
    protected void adjustDelimiter(ParserContext context, StatementType statementType) {
        if (statementType == PLSQL_STATEMENT || statementType == PLSQL_VIEW_STATEMENT || statementType == PLSQL_JAVA_STATEMENT
                || statementType == PLSQL_PACKAGE_BODY_STATEMENT) {
            context.setDelimiter(PLSQL_DELIMITER);




        } else {
            context.setDelimiter(Delimiter.SEMICOLON);
        }
    }













    @Override
    protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
        // Package bodies can have an unbalanced BEGIN without END in the initialisation section.
        TokenType tokenType = token.getType();
        if (context.getStatementType() == PLSQL_PACKAGE_BODY_STATEMENT && (TokenType.EOF == tokenType || TokenType.DELIMITER == tokenType)) {
            return true;
        }

        // Handle wrapped SQL on these token types to ensure it gets treated as one block
        if (context.getStatementType() == PLSQL_WRAPPED_STATEMENT && (TokenType.EOF == tokenType || TokenType.DELIMITER == tokenType)) {
            return true;
        }

        // In Oracle, symbols { } affect the block depth in embedded Java code
        if (token.getType() == TokenType.SYMBOL && context.getStatementType() == PLSQL_JAVA_STATEMENT) {
            return true;
        }








        return super.shouldAdjustBlockDepth(context, tokens, token);
    }

    // These words increase the block depth - unless preceded by END (in which case the END will decrease the block depth)
    private static final List<String> CONTROL_FLOW_KEYWORDS = Arrays.asList("IF", "LOOP", "CASE");

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
        TokenType tokenType = keyword.getType();
        String keywordText = keyword.getText();
        int parensDepth = keyword.getParensDepth();

        if (lastTokenIs(tokens, parensDepth, "GOTO")) {
            return;
        }

        if (context.getStatementType() == PLSQL_WRAPPED_STATEMENT) {
            // ensure wrapped SQL has an increased block depth so it gets treated as one statement
            if (context.getBlockDepth() == initialWrappedBlockDepth) {
                context.increaseBlockDepth("WRAPPED");
            }
            // decrease block depth at the end to step out of a wrapped SQL block
            if (TokenType.EOF == tokenType && context.getBlockDepth() > 0) {
                context.decreaseBlockDepth();
            }
            // return early as we don't need to parse the contents of wrapped SQL - it's all one statement anyways
            return;
        } else {
            // decrease block depth when wrapped SQL ends to step out of wrapped SQL block
            if (context.getBlockDepth() > initialWrappedBlockDepth && context.getBlockInitiator().equals("WRAPPED")) {
                initialWrappedBlockDepth = -1;
                context.decreaseBlockDepth();
            }
        }

        // In embedded Java code we judge the end of a class definition by the depth of braces.
        // We ignore normal SQL keywords as Java code can contain arbitrary identifiers.
        if (context.getStatementType() == PLSQL_JAVA_STATEMENT) {
            if ("{".equals(keywordText)) {
                context.increaseBlockDepth("PLSQL_JAVA_STATEMENT");
            } else if ("}".equals(keywordText)) {
                context.decreaseBlockDepth();
            }
            return;
        }

        if ("BEGIN".equals(keywordText)
                || (CONTROL_FLOW_KEYWORDS.contains(keywordText) && !precedingEndAttachesToThisKeyword(tokens, parensDepth, context, keyword))
                || ("TRIGGER".equals(keywordText) && lastTokenIs(tokens, parensDepth, "COMPOUND"))
                || (context.getBlockDepth() == 0 && (
                doTokensMatchPattern(tokens, keyword, PLSQL_PACKAGE_BODY_REGEX) ||
                        doTokensMatchPattern(tokens, keyword, PLSQL_PACKAGE_DEFINITION_REGEX) ||
                        doTokensMatchPattern(tokens, keyword, PLSQL_TYPE_BODY_REGEX)))
        ) {
            context.increaseBlockDepth(keywordText);
        } else if ("END".equals(keywordText)) {
            context.decreaseBlockDepth();
        }

        // Package bodies can have an unbalanced BEGIN without END in the initialisation section. This allows us
        // to exit the package even though we are still at block depth 1 due to the BEGIN.
        if (context.getStatementType() == PLSQL_PACKAGE_BODY_STATEMENT && (TokenType.EOF == tokenType || TokenType.DELIMITER == tokenType) && context.getBlockDepth() == 1) {
            context.decreaseBlockDepth();
        }
    }

    private boolean precedingEndAttachesToThisKeyword(List<Token> tokens, int parensDepth, ParserContext context, Token keyword) {
        // Normally IF, LOOP and CASE all pair up with END IF, END LOOP, END CASE
        // However, CASE ... END is valid in expressions, so in code such as
        //      FOR i IN 1 .. CASE WHEN foo THEN 5 ELSE 6 END
        //      LOOP
        //        ...
        //      END LOOP
        // the first END does *not* attach to the subsequent LOOP. The same is possible with $IF ... $END constructions
        return lastTokenIs(tokens, parensDepth, "END") &&
                lastTokenIsOnLine(tokens, parensDepth, keyword.getLine()) &&
                keyword.getText().equals(context.getLastClosedBlockInitiator());
    }

    @Override
    protected boolean doTokensMatchPattern(List<Token> previousTokens, Token current, Pattern regex) {
        if (regex == PLSQL_PACKAGE_DEFINITION_REGEX &&
                previousTokens.stream().anyMatch(t -> t.getType() == TokenType.KEYWORD && t.getText().equalsIgnoreCase("ACCESSIBLE"))) {
            ArrayList<String> tokenStrings = new ArrayList<>();
            tokenStrings.add(current.getText());

            for (int i = previousTokens.size() - 1; i >= 0; i--) {
                Token prevToken = previousTokens.get(i);
                if (prevToken.getType() == TokenType.KEYWORD) {
                    tokenStrings.add(prevToken.getText());
                }
            }

            StringBuilder builder = new StringBuilder();
            for (int i = tokenStrings.size() - 1; i >= 0; i--) {
                builder.append(tokenStrings.get(i));
                if (i != 0) {
                    builder.append(" ");
                }
            }

            return regex.matcher(builder.toString()).matches() || super.doTokensMatchPattern(previousTokens, current, regex);
        }

        return super.doTokensMatchPattern(previousTokens, current, regex);
    }

    @Override
    protected boolean isDelimiter(String peek, ParserContext context, int col, int colIgnoringWhitespace) {
        Delimiter delimiter = context.getDelimiter();

        if (peek.startsWith(delimiter.getEscape() + delimiter.getDelimiter())) {
            return true;
        }

        if (delimiter.shouldBeAloneOnLine()) {
            // Only consider alone-on-line delimiters (such as "/" for PL/SQL) if
            // it's the first character on the line
            if (colIgnoringWhitespace == 1 && peek == delimiter.getDelimiter()) {
                return true;
            }

            if (colIgnoringWhitespace != 1) {
                return false;
            }
        } else {
            if (colIgnoringWhitespace == 1 && "/".equals(peek.trim())) {
                return true;
            }
        }

        return super.isDelimiter(peek, context, col, colIgnoringWhitespace);
    }














    @Override
    protected boolean isAlternativeStringLiteral(String peek) {
        if (peek.length() < 3) {
            return false;
        }
        // Oracle's quoted-literal syntax is introduced by q (case-insensitive) followed by a literal surrounded by
        // any of !!, [], {}, (), <> provided the selected pair do not appear in the literal string; the others may do.
        char firstChar = peek.charAt(0);
        return (firstChar == 'q' || firstChar == 'Q') && peek.charAt(1) == '\'';
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
            case '!':
                return "!'";
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