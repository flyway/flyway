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
package com.googlecode.flyway.core.migration.sql;

import com.googlecode.flyway.core.exception.FlywayException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public class SqlStatement {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SqlStatement.class);

    /**
     * The original line number where the statement was located in the script it came from.
     */
    private int lineNumber;

    /**
     * The sql to send to the database.
     */
    private String sql;

    /**
     * Creates a new sql statement.
     *
     * @param lineNumber The original line number where the statement was located in the script it came from.
     * @param sql The sql to send to the database.
     */
    public SqlStatement(int lineNumber, String sql) {
        this.lineNumber = lineNumber;
        this.sql = sql;
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
     * Executes this statement against the database.
     *
     * @param jdbcTemplate        The jdbc template to use to execute this statement.
     */
    public void execute(JdbcTemplate jdbcTemplate) {
        LOG.debug("Executing SQL: " + this.sql);
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            throw new FlywayException("Error executing statement at line " + lineNumber
                    + ": " + sql, e);
        }
    }
}
