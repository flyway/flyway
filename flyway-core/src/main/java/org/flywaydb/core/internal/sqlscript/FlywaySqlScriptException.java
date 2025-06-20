/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import java.sql.SQLException;
import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.exception.FlywaySqlException;

/**
 * This specific exception thrown when Flyway encounters a problem in SQL script
 */
@CustomLog
public class FlywaySqlScriptException extends FlywaySqlException {
    /**
     * The resource containing the failed statement.
     */
    @Getter
    private final Resource resource;

    private final SqlStatement statement;

    private final String decoratedMessage;

    public static final String STATEMENT_MESSAGE = "Run Flyway with -X option to see the actual statement causing the problem";

    /**
     * Creates new instance of FlywaySqlScriptException.
     *
     * @param resource     The resource containing the failed statement.
     * @param statement    The failed SQL statement.
     * @param sqlException Cause of the problem.
     */
    public FlywaySqlScriptException(final Resource resource,
        final SqlStatement statement,
        final SQLException sqlException,
        final String environment) {
        super(generateMessage(resource, environment), sqlException);
        this.resource = resource;
        this.statement = statement;

        final StringBuilder builder = new StringBuilder(super.getMessage());
        if (resource != null) {
            builder.append("Location   : ")
                .append(resource.getAbsolutePath())
                .append(" (")
                .append(resource.getAbsolutePathOnDisk())
                .append(")\n");
        }
        if (statement != null) {
            builder.append("Line       : ").append(getLineNumber()).append("\n");
            builder.append("Statement  : ")
                .append(LOG.isDebugEnabled() ? getStatement() : STATEMENT_MESSAGE)
                .append("\n");
        }
        this.decoratedMessage = builder.toString();
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
        return decoratedMessage;
    }

    private static String generateMessage(final Resource resource, final String environment) {
        final StringBuilder messageBuilder = new StringBuilder("Failed to execute script");

        if (resource != null) {
            messageBuilder.append(" ").append(resource.getFilename());
        }

        if (!"default".equalsIgnoreCase(environment)) {
            messageBuilder.append(" against ").append(environment).append(" environment");
        }

        return messageBuilder.toString();
    }
}
