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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.Delimiter;

/**
 * @author artem
 * @date 14.09.2020
 * @time 17:07
 */
public class ExasolParser extends Parser {

    public ExasolParser(final Configuration configuration, final ParsingContext parsingContext) {
        super(configuration, parsingContext, 3);
    }

    @Override
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter(";", true);
    }
}
