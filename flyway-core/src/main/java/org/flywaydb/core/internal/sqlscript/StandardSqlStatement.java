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
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.line.Line;

import java.util.List;

/**
 * A SQL statement from a script that can be executed at once against a database.
 */
public class StandardSqlStatement extends AbstractSqlStatement {
    public StandardSqlStatement(List<Line> lines, Delimiter delimiter



    ) {
        super(lines, delimiter



        );
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate, SqlScriptExecutor sqlScriptExecutor) {
        return jdbcTemplate.executeStatement(getSql());
    }
}