package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.configuration.Configuration;

/**
 * Executor for SQL scripts.
 */
public interface SqlScriptExecutor {
    /**
     * Executes this SQL script.
     *
     * @param sqlScript The SQL script.
     */
    void execute(SqlScript sqlScript, Configuration config);
}