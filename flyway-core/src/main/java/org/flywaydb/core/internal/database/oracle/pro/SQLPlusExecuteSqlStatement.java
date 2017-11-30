/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ErrorContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * A SQL*Plus EXECUTE statement.
 */
public class SQLPlusExecuteSqlStatement extends AbstractSqlStatement {
    public SQLPlusExecuteSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public void execute(ErrorContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        jdbcTemplate.executeStatement(errorContext, transformSql());
    }

    String transformSql() {
        String noExecute = sql.substring(sql.indexOf(" ") + 1);
        String noDash = noExecute.replaceAll("-\n", "\n");
        return "BEGIN\n" + noDash + ";\nEND;";
    }
}
