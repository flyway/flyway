/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.database.sqlserver;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SQLServerParser extends Parser {
    // #2175, 2298, 2542: Various system sprocs, mostly around replication, cannot be executed within a transaction.
    // These procedures are only present in SQL Server. Not on Azure nor in PDW.
    private static final List<String> SPROCS_INVALID_IN_TRANSACTIONS = Arrays.asList(
            "SP_ADDSUBSCRIPTION", "SP_DROPSUBSCRIPTION",
            "SP_ADDDISTRIBUTOR", "SP_DROPDISTRIBUTOR",
            "SP_ADDDISTPUBLISHER", "SP_DROPDISTPUBLISHER",
            "SP_ADDLINKEDSERVER", "SP_DROPLINKEDSERVER",
            "SP_ADDLINKEDSRVLOGIN", "SP_DROPLINKEDSRVLOGIN",
            "SP_SERVEROPTION", "SP_REPLICATIONDBOPTION",
            "SP_FULLTEXT_DATABASE");

    private static final Pattern TRANSACTION_REGEX = Pattern.compile("TRAN(SACTION)?");

    public SQLServerParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected boolean isDelimiter(String peek, ParserContext context, int col, int colIgnoringWhitepace) {
        return peek.length() >= 2
                && (peek.charAt(0) == 'G' || peek.charAt(0) == 'g')
                && (peek.charAt(1) == 'O' || peek.charAt(1) == 'o')
                && (peek.length() == 2 || Character.isWhitespace(peek.charAt(2)));
    }

    @Override
    protected String readKeyword(PeekingReader reader, Delimiter delimiter, ParserContext context) throws IOException {
        // #2414: Ignore delimiter as GO (unlike ;) can be part of a regular keyword
        return "" + (char) reader.read() + reader.readKeywordPart(null, context);
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
        if (keywords.size() == 0) {
            return null;
        }

        Token currentToken = keywords.get(keywords.size() - 1);
        String current = currentToken.getText();

        if (currentToken.getType() != TokenType.IDENTIFIER &&
                ("BACKUP".equals(current) || "RESTORE".equals(current) || "RECONFIGURE".equals(current))) {
            return false;
        }

        if (keywords.size() < 2) {
            return null;
        }

        String previous = keywords.get(keywords.size() - 2).getText();

        if ("EXEC".equals(previous) && SPROCS_INVALID_IN_TRANSACTIONS.contains(current)) {
            return false;
        }

        // (CREATE|DROP|ALTER) (DATABASE|FULLTEXT (INDEX|CATALOG))
        if (("CREATE".equals(previous) || "ALTER".equals(previous) || "DROP".equals(previous))
                && ("DATABASE".equals(current) || "FULLTEXT".equals(current))) {
            return false;
        }

        return null;
    }

    @Override
    protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
        TokenType tokenType = token.getType();
        if (TokenType.DELIMITER.equals(tokenType) || ";".equals(token.getText())) {
            return true;
        } else if (TokenType.EOF.equals(tokenType)) {
            return true;
        }

        return super.shouldAdjustBlockDepth(context, tokens, token);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        String keywordText = keyword.getText();

        if ("BEGIN".equals(keywordText)) {
            context.increaseBlockDepth("");
        }

        if (context.getBlockDepth() > 0 && ("END".equals(keywordText) ||
                isTransaction(tokens, keyword, keywordText) ||
                isDistributedTransaction(tokens, keyword, keywordText))) {
            context.decreaseBlockDepth();
        }

        super.adjustBlockDepth(context, tokens, keyword, reader);
    }

    private boolean isTransaction(List<Token> tokens, Token keyword, String keywordText) {
        return TRANSACTION_REGEX.matcher(keywordText).matches() &&
                lastTokenIs(tokens, keyword.getParensDepth(), "BEGIN");
    }

    private boolean isDistributedTransaction(List<Token> tokens, Token keyword, String keywordText) {
        return TRANSACTION_REGEX.matcher(keywordText).matches() &&
                lastTokenIs(tokens, keyword.getParensDepth(), "DISTRIBUTED") &&
                tokenAtIndexIs(tokens, tokens.size() - 2, "BEGIN");
    }

    @Override
    protected int getTransactionalDetectionCutoff() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected char getOpeningIdentifierSymbol() {
        return '[';
    }

    @Override
    protected char getClosingIdentifierSymbol() {
        return ']';
    }
}