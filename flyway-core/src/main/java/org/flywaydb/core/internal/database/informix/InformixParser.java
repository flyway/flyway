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
package org.flywaydb.core.internal.database.informix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.Token;

import java.util.List;

public class InformixParser extends Parser {
    public InformixParser(Configuration configuration) {
        super(configuration, 2);
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> keywords) {
        if (keywords.size() < 2) {
            return;
        }
        String current = keywords.get(keywords.size() - 1).getText();
        if ("FUNCTION".equals(current) || "PROCEDURE".equals(current)) {
            String previous = keywords.get(keywords.size() - 2).getText();

            // CREATE( DBA)? (FUNCTION|PROCEDURE)
            if ("CREATE".equals(previous) || "DBA".equals(previous)) {
                context.increaseBlockDepth();
            } else if ("END".equals(previous)) {
                context.decreaseBlockDepth();
            }
        }
    }
}