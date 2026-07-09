/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.List;

public class SQLiteParser extends Parser {
    public SQLiteParser(final Configuration configuration, final ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected char getAlternativeIdentifierQuote() {
        return '`';
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(final String simplifiedStatement, final List<Token> keywords) {
        if ("PRAGMA FOREIGN_KEYS".equals(simplifiedStatement)) {
            return false;
        }

        return null;
    }

    @Override
    protected void adjustBlockDepth(final ParserContext context,
        final List<Token> tokens,
        final Token keyword,
        final PeekingReader reader) throws IOException {
        final String lastKeyword = keyword.getText();
        if ("BEGIN".equals(lastKeyword) || "CASE".equals(lastKeyword)) {
            context.increaseBlockDepth(lastKeyword);
        } else if ("END".equals(lastKeyword)) {
            context.decreaseBlockDepth();
        }
    }
}
