/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.exasol;

import java.io.IOException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.StatementType;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;
import org.flywaydb.core.internal.sqlscript.Delimiter;

/**
 * @author artem
 * @date 14.09.2020
 * @time 17:07
 */
public class ExasolParser extends Parser {

    public static final StatementType SCRIPT = new StatementType();
    public static final String CREATE_SCRIPT_REGEXP
      = "create\\s+(or\\s+replace\\s+)*(python|python3|lua|java|r|script).*";
    public static final String SCRIPT_DELIMITER = "/";

    public ExasolParser(final Configuration configuration, final ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter(";", true);
    }

    @Override
    protected boolean isDelimiter(final String peek, final ParserContext context, final int col) {

        if (context.getStatementType().equals(SCRIPT)) {
            return peek.startsWith(SCRIPT_DELIMITER);
        }

        return super.isDelimiter(peek, context, col);
    }

    @Override
    protected StatementType detectStatementType(final String simplifiedStatement) {

        if (simplifiedStatement.toLowerCase().matches(CREATE_SCRIPT_REGEXP)) {
            return SCRIPT;
        }

        return super.detectStatementType(simplifiedStatement);
    }

    @Override
    protected Token handleDelimiter(final PeekingReader reader, final ParserContext context, final int pos,
                                    final int line, final int col)
      throws IOException {

        final String text
          = context.getStatementType().equals(SCRIPT) ? SCRIPT_DELIMITER : context.getDelimiter().getDelimiter();

        reader.swallow(text.length());

        return new Token(TokenType.DELIMITER, pos, line, col, text, text, context.getParensDepth());
    }
}
