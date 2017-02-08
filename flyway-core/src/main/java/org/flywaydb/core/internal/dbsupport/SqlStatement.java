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
package org.flywaydb.core.internal.dbsupport;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public class SqlStatement {
    /**
     * The original line number where the statement was located in the script it came from.
     */
    private int lineNumber;

    /**
     * The sql to send to the database.
     */
    private String sql;

    /**
     * Whether this is a PostgreSQL COPY FROM STDIN statement.
     * <p/>
     * Note: This may have to be generalized if additional special cases appear.
     */
    private boolean pgCopy;

    /**
     * Creates a new sql statement.
     *
     * @param lineNumber The original line number where the statement was located in the script it came from.
     * @param sql        The sql to send to the database.
     * @param pgCopy     Whether this is a PostgreSQL COPY FROM STDIN statement.
     */
    public SqlStatement(int lineNumber, String sql, boolean pgCopy) {
        this.lineNumber = lineNumber;
        this.sql = sql;
        this.pgCopy = pgCopy;
    }

    /**
     * @return The original line number where the statement was located in the script it came from.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return The sql to send to the database.
     */
    public String getSql() {
        return sql;
    }

    /**
     * @return Whether this is a PostgreSQL COPY FROM STDIN statement.
     */
    public boolean isPgCopy() {
        return pgCopy;
    }
}
