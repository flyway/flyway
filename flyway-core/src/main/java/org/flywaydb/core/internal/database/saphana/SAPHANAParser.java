/*
 * Copyright 2010-2020 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class SAPHANAParser extends Parser {
    private static final StatementType SQLSCRIPT_STATEMENT = new StatementType();

    private static final Pattern SQLSCRIPT_REGEX = Pattern.compile(
            "^CREATE(\\sOR\\sREPLACE)?\\s(FUNCTION|PROCEDURE)");

    public SAPHANAParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    protected boolean tokensContain(List<Token> tokens, String tokenText) {
        for (int i = tokens.size()-1; i >= 0; i--) {
            Token previousToken = tokens.get(i);
            if (tokenText.equals(previousToken.getText())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        int lastKeywordIndex = getLastKeywordIndex(tokens);
        if (lastKeywordIndex < 0) {
            return;
        }
        Token previousKeyword = tokens.get(lastKeywordIndex);

        if ( "PROCEDURE".equals(keyword.getText()) || "FUNCTION".equals(keyword.getText()) ) {
            String previous = tokens.get(lastKeywordIndex).getText();

            if ("CREATE".equals(previous) || "REPLACE".equals(previous)) {
                context.increaseBlockDepth();
            }
        }


        // BEGIN, CASE, DO and IF increases block depth
        if ("BEGIN".equals(keyword.getText()) || "CASE".equals(keyword.getText()) || "DO".equals(keyword.getText()) || "IF".equals(keyword.getText())

                // But not END IF
                && !"END".equals(previousKeyword.getText())) {
            context.increaseBlockDepth();
        } else if ("END".equals(keyword.getText())) {
            context.decreaseBlockDepth();
        }

        if (context.getStatementType() == SQLSCRIPT_STATEMENT && tokensContain(tokens, "BEGIN")
                && "END".equals(keyword.getText()) && context.getBlockDepth() == 1) {
            context.decreaseBlockDepth();
            return;
        }
    }

    @Override
    protected StatementType detectStatementType(String simplifiedStatement) {
        if (SQLSCRIPT_REGEX.matcher(simplifiedStatement).matches()) {
            return SQLSCRIPT_STATEMENT;
        }

        return super.detectStatementType(simplifiedStatement);
    }
}
