package org.flywaydb.core.internal.sqlscript;

import java.sql.Connection;

public interface SqlScriptExecutorFactory {
    /**
     * Creates a new executor for this SQL script.
     *
     * @return A new SQL script executor.
     */
    SqlScriptExecutor createSqlScriptExecutor(Connection connection, boolean undo, boolean batch, boolean outputQueryResults);
}