/*-
 * ========================LICENSE_START=================================
 * flyway-database-informix
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
package org.flywaydb.database.informix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.List;

public class InformixParser extends Parser {
    public InformixParser(final Configuration configuration, final ParsingContext parsingContext) {
        super(configuration, parsingContext, 2);
    }

    @Override
    protected void adjustBlockDepth(final ParserContext context,
        final List<Token> tokens,
        final Token keyword,
        final PeekingReader reader) throws IOException {
        final int lastKeywordIndex = getLastKeywordIndex(tokens);
        if (lastKeywordIndex < 0) {
            return;
        }

        final String current = keyword.getText();
        if ("FUNCTION".equals(current) || "PROCEDURE".equals(current)) {
            final String previous = tokens.get(lastKeywordIndex).getText();

            // CREATE( DBA)? (FUNCTION|PROCEDURE)
            if ("CREATE".equals(previous) || "DBA".equals(previous)) {
                context.increaseBlockDepth(previous);
            } else if ("END".equals(previous)) {
                context.decreaseBlockDepth();
            }
        }
    }
}
