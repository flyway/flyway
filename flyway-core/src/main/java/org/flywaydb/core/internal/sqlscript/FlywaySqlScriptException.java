package org.flywaydb.core.internal.sqlscript;

import lombok.Getter;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;

/**
 * This specific exception thrown when Flyway encounters a problem in SQL script
 */
public class FlywaySqlScriptException extends FlywaySqlException {
    /**
     * @return The resource containing the failed statement.
     */
    @Getter
    private final Resource resource;

    private final SqlStatement statement;

    /**
     * Creates new instance of FlywaySqlScriptException.
     *
     * @param resource The resource containing the failed statement.
     * @param statement The failed SQL statement.
     * @param sqlException Cause of the problem.
     */
    public FlywaySqlScriptException(Resource resource, SqlStatement statement, SQLException sqlException) {
        super(resource == null ? "Script failed" : "Migration " + resource.getFilename() + " failed", sqlException);
        this.resource = resource;
        this.statement = statement;
    }

    /**
     * Returns the line number in migration SQL script where exception occurred.
     *
     * @return The line number.
     */
    public int getLineNumber() {
        return statement == null ? -1 : statement.getLineNumber();
    }

    /**
     * Returns the failed statement in SQL script.
     *
     * @return The failed statement.
     */
    public String getStatement() {
        return statement == null ? "" : statement.getSql();
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (resource != null) {
            message += "Location   : " + resource.getAbsolutePath() + " (" + resource.getAbsolutePathOnDisk() + ")\n";
        }
        if (statement != null) {
            message += "Line       : " + getLineNumber() + "\n";
            message += "Statement  : " + getStatement() + "\n";
        }
        return message;
    }
}