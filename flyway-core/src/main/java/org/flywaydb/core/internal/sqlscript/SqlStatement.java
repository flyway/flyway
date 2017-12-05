/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.Result;

import java.sql.SQLException;
import java.util.List;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public interface SqlStatement {
    /**
     * @return The original line number where the statement was located in the script it came from.
     */
    int getLineNumber();

    /**
     * @return The sql to send to the database.
     */
    String getSql();

    /**
     * Executes this statement against the database.
     *
     * @param errorContext The error context.
     * @param jdbcTemplate The jdbcTemplate to use to execute this script.
     * @throws SQLException when the execution fails.
     */
    List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException;
}
