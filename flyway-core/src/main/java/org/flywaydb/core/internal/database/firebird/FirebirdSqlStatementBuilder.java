/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

/**
 * FirebirdSqlStatementBuilder
 */
public class FirebirdSqlStatementBuilder extends SqlStatementBuilder {

    /**
     * Holds the beginning of the statement.
     */

    private String statementStart = "";

    private String startDeliminator = "SET TERM(.*)";

    FirebirdSqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        //If we find SET TERM then change the delimiter
        if (line.toUpperCase().matches(startDeliminator)) {
            return new Delimiter(line.substring("SET TERM".length(), line.indexOf(this.delimiter.getDelimiter())).trim(), false);
        }

        return null;
    }



}