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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.StandardSqlStatement;
import org.flywaydb.core.internal.line.Line;

import java.util.List;

/**
 * An Oracle PL/SQL statement.
 */
public class OracleWithPLSQLStatement extends StandardSqlStatement {
    /**
     * Creates a new sql statement.
     *
     * @param lines The lines of the statement.
     */
    OracleWithPLSQLStatement(List<Line> lines) {
        super(lines, OracleSqlStatementBuilder.PLSQL_DELIMITER



        );
    }

    @Override
    protected void stripDelimiter(StringBuilder sql, Delimiter delimiter) {
        super.stripDelimiter(sql, delimiter);

        // Strip extra semicolon to avoid issues with WITH statements containing PL/SQL
        stripTrailingWhitespace(sql);
        if (';' == sql.charAt(sql.length() - 1)) {
            sql.delete(sql.length() - 1, sql.length());
        }
    }
}