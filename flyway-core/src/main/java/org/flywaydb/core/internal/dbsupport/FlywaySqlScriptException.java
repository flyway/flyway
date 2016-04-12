/**
 * Copyright 2010-2016 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.sql.SQLException;

/**
 * This specific exception thrown when Flyway encounters a problem in SQL script
 */
public class FlywaySqlScriptException extends FlywayException {

    private final Resource resource;
    private final SqlStatement statement;

    /**
     * Creates new instance of FlywaySqlScriptException.
     *
     * @param resource     The resource containg the failed statement.
     * @param statement    The failed SQL statement.
     * @param sqlException Cause of the problem.
     */
    public FlywaySqlScriptException(Resource resource, SqlStatement statement, SQLException sqlException) {
        super(sqlException);
        this.resource = resource;
        this.statement = statement;
    }

    /**
     * Returns the line number in migration SQL script where exception occurred.
     *
     * @return The line number.
     */
    public int getLineNumber() {
        return statement.getLineNumber();
    }

    /**
     * Returns the failed statement in SQL script.
     *
     * @return The failed statement.
     */
    public String getStatement() {
        return statement.getSql();
    }

    @Override
    public String getMessage() {
        String title = resource == null ? "Script failed" : "Migration " + resource.getFilename() + " failed";
        String underline = StringUtils.trimOrPad("", title.length(), '-');

        SQLException cause = (SQLException) getCause();
        while (cause.getNextException() != null) {
            cause = cause.getNextException();
        }

        String message = "\n" + title + "\n" + underline + "\n";
        message += "SQL State  : " + cause.getSQLState() + "\n";
        message += "Error Code : " + cause.getErrorCode() + "\n";
        if (cause.getMessage() != null) {
            message += "Message    : " + cause.getMessage().trim() + "\n";
        }
        if (resource != null) {
            message += "Location   : " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")\n";
        }
        message += "Line       : " + getLineNumber() + "\n";
        message += "Statement  : " + getStatement() + "\n";

        return message;
    }
}
