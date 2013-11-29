package com.googlecode.flyway.core.api;

import java.sql.SQLException;

/**
 * This specific exception thrown when Flyway encounters a problem in SQL script
 */
public class FlywaySqlScriptException extends FlywayException {

    private final int lineNumber;
    private final CharSequence statement;

    /**
     * Creates new instance of FlywaySqlScriptException.
     *
     * @param lineNumber   The line number in SQL script where exception occurred.
     * @param statement    The failed SQL statement.
     * @param sqlException Cause of the problem.
     */
    public FlywaySqlScriptException(int lineNumber, String statement, SQLException sqlException) {
        super("Error executing statement at line " + lineNumber + ": " + statement, sqlException);

        this.lineNumber = lineNumber;
        this.statement = statement;
    }

    /**
     * Returns the line number in migration SQL script where exception occurred.
     *
     * @return The line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the failed statement in SQL script.
     *
     * @return The failed statement.
     */
    public CharSequence getStatement() {
        return statement;
    }
}
