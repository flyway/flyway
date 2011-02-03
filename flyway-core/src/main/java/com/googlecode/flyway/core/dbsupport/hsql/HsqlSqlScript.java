/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import org.springframework.util.StringUtils;

/**
 * SqlScript supporting Hsql-specific delimiter changes.
 */
public class HsqlSqlScript extends SqlScript {
    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     *
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public HsqlSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        super(sqlScriptSource, placeholderReplacer);
    }

    @Override
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter) {
        // Check whether we are inside a string literal or not.
        // Hsql only supports single quotes (') as delimiters
        // A single quote inside a string literal is represented as two single quotes ('')
        // An even number of single quotes thus means the string literal is closed.
        // An uneven number means we are still waiting for the closing delimiter on a following line
        int numQuotes = StringUtils.countOccurrencesOf(statement, "'");
        if ((numQuotes % 2) == 0) {
            // String literal is closed.
            return DEFAULT_STATEMENT_DELIMITER;
        }

        // Still inside the string literal
        return null;
    }
}
