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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.sqlscript.SqlStatement;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public abstract class AbstractSqlStatement implements SqlStatement {
    /**
     * The original line number where the statement was located in the script it came from.
     */
    protected int lineNumber;

    /**
     * The sql to send to the database.
     */
    protected String sql;

    public AbstractSqlStatement(int lineNumber, String sql) {
        this.lineNumber = lineNumber;
        this.sql = sql;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
