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
package com.googlecode.flyway.core.dbsupport.mssql;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;

/**
 * SqlScript supporting MSSQL-specific delimiter changes.
 */
public class MSSQLSqlScript extends SqlScript {
    private static final String DELIMITER = "GO";

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     *
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public MSSQLSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        super(sqlScriptSource, placeholderReplacer);
    }

    @Override
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter) {
        return DELIMITER;
//        String upperCaseLine = line.toUpperCase();
//        if (upperCaseLine.startsWith("CREATE")
//                && (upperCaseLine.contains("FUNCTION") || upperCaseLine.contains("PROCEDURE")) || upperCaseLine.contains("TABLE") || upperCaseLine.contains("VIEW")) {
//            return DELIMITER;
//        }
//
//        if (upperCaseLine.matches("(ALTER|DROP)\\s*TABLE")) {
//            return DELIMITER;
//        }
//
//        return delimiter;
    }
}
