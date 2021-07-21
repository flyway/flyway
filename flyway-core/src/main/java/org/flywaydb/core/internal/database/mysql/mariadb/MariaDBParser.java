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
package org.flywaydb.core.internal.database.mysql.mariadb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.mysql.MySQLParser;
import org.flywaydb.core.internal.parser.*;

import java.util.List;
import java.util.regex.Pattern;

public class MariaDBParser extends MySQLParser {
    private static final Pattern BEGIN_NOT_ATOMIC_REGEX = Pattern.compile(
            "^BEGIN\\sNOT\\sATOMIC\\s.*END", Pattern.CASE_INSENSITIVE);
    private static final StatementType BEGIN_NOT_ATOMIC_STATEMENT = new StatementType();

    public MariaDBParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext);
    }

    @Override
    protected StatementType detectStatementType(String simplifiedStatement, ParserContext context) {
        if (BEGIN_NOT_ATOMIC_REGEX.matcher(simplifiedStatement).matches()) {
            return BEGIN_NOT_ATOMIC_STATEMENT;
        }

        return super.detectStatementType(simplifiedStatement, context);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
        String keywordText = keyword.getText();

        if (lastTokenIs(tokens, context.getParensDepth(), "NOT") && "ATOMIC".equalsIgnoreCase(keywordText)) {
            context.increaseBlockDepth("");
        }

        if (context.getBlockDepth() > 0 && context.getStatementType() == BEGIN_NOT_ATOMIC_STATEMENT &&
                keywordText.equalsIgnoreCase("END")) {
            context.decreaseBlockDepth();
        }

        super.adjustBlockDepth(context, tokens, keyword, reader);
    }
}