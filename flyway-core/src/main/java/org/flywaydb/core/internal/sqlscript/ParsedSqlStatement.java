/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.sqlscript;

import lombok.Getter;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public class ParsedSqlStatement implements SqlStatement {
    @Getter
    private final int pos;
    @Getter
    private final int line;
    @Getter
    private final int col;
    @Getter(onMethod = @__(@Override))
    private final String sql;

    /**
     * The delimiter of the statement.
     */
    private final Delimiter delimiter;

    private final boolean canExecuteInTransaction;

    /**
     * Whether this statement can be run as part of batch or whether it must be run individually.
     */
    @Getter(onMethod = @__(@Override))
    private final boolean batchable;

    public ParsedSqlStatement(int pos, int line, int col, String sql, Delimiter delimiter,
                              boolean canExecuteInTransaction, boolean batchable) {
        this.pos = pos;
        this.line = line;
        this.col = col;
        this.sql = sql;
        this.delimiter = delimiter;
        this.canExecuteInTransaction = canExecuteInTransaction;
        this.batchable = batchable;
    }

    @Override
    public final int getLineNumber() {
        return line;
    }

    @Override
    public String getDelimiter() {
        return delimiter.toString();
    }

    @Override
    public boolean canExecuteInTransaction() {
        return canExecuteInTransaction;
    }








    @Override
    public Results execute(JdbcTemplate jdbcTemplate




                          ) {
        return jdbcTemplate.executeStatement(sql);
    }
}
