/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.db2;

import com.googlecode.flyway.core.migration.sql.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

/**
 * SqlStatementBuilder supporting DB2-specific delimiter changes.
 * <p/>
 * TODO Support for Procedures.
 */
public class DB2SqlStatementBuilder extends SqlStatementBuilder {
    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        // DB2 only supports single quotes (') as delimiters
        // A single quote inside a string literal is represented as two single quotes ('')
        // An even number of single quotes thus means the string literal is closed.
        // An uneven number means we are still waiting for the closing delimiter on a following line
        int numQuotes = StringUtils.countOccurrencesOf(statement, "'");
        return (numQuotes % 2) != 0;
    }
}
