/*-
 * ========================LICENSE_START=================================
 * flyway-database-databricks
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
package org.flywaydb.database.databricks;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DatabricksParser extends Parser {
    private static final List<String> CONDITIONALLY_CREATABLE_OBJECTS = Arrays.asList("CATALOG",
        "CONNECTION",
        "DATABASE",
        "FUNCTION",
        "SCHEMA",
        "TABLE",
        "VIEW",
        "VOLUME");

    public DatabricksParser(final Configuration configuration, final ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected void adjustBlockDepth(final ParserContext context,
        final List<Token> tokens,
        final Token keyword,
        final PeekingReader reader) throws IOException {
        final int lastKeywordIndex = getLastKeywordIndex(tokens);
        final Token previousKeyword = lastKeywordIndex >= 0 ? tokens.get(lastKeywordIndex) : null;
        final String keywordText = keyword.getText();
        final String previousKeywordText = previousKeyword != null ? previousKeyword.getText()
            .toUpperCase(Locale.ENGLISH) : "";

        if ("BEGIN".equalsIgnoreCase(keywordText) && (reader.peekIgnoreCase(" TRANSACTION") || reader.peekIgnoreCase(
            " WORK"))) {
            return;
        }

        if ("BEGIN".equalsIgnoreCase(keywordText) || (("CASE".equalsIgnoreCase(keywordText)
            || ("IF".equalsIgnoreCase(keywordText) && !CONDITIONALLY_CREATABLE_OBJECTS.contains(previousKeywordText))
            || "FOR".equalsIgnoreCase(keywordText)
            || "WHILE".equalsIgnoreCase(keywordText)
            || "LOOP".equalsIgnoreCase(keywordText)
            || "REPEAT".equalsIgnoreCase(keywordText)) && previousKeyword != null && !(lastKeywordIndex
            == tokens.size() - 1 && "END".equalsIgnoreCase(previousKeywordText)) && !"CURSOR".equalsIgnoreCase(
            previousKeywordText))) {
            context.increaseBlockDepth(keywordText);
        } else if ("END".equalsIgnoreCase(keywordText) && context.getBlockDepth() > 0) {
            context.decreaseBlockDepth();
        }
    }
}
