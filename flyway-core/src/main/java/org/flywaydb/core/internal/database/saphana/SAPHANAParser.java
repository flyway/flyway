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
package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.Token;

import java.util.List;

public class SAPHANAParser extends Parser {
    public SAPHANAParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword) {
        int lastKeywordIndex = getLastKeywordIndex(tokens);
        if (lastKeywordIndex < 0) {
            return;
        }
        Token previousKeyword = tokens.get(lastKeywordIndex);

        // BEGIN, DO and IF increases block depth
        if (("BEGIN".equals(keyword.getText()) || "DO".equals(keyword.getText()) || "IF".equals(keyword.getText())
                // But not END FOR, END IF and END WHILE
                && !"END".equals(previousKeyword.getText()))) {
            context.increaseBlockDepth();
        } else if ("END".equals(keyword.getText())) {
            context.decreaseBlockDepth();
        }
    }
}