/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * This specific exception thrown when Flyway encounters a problem in SQL statement.
 */
public class FlywaySqlException extends FlywayException {
    /**
     * Creates new instance of FlywaySqlScriptException.
     *
     * @param sqlException Cause of the problem.
     */
    public FlywaySqlException(String message, SQLException sqlException) {
        super(message, sqlException);
    }

    @Override
    public String getMessage() {
        String title = super.getMessage();
        String underline = StringUtils.trimOrPad("", title.length(), '-');

        return "\n" + title + "\n" + underline + "\n" + ExceptionUtils.toMessage((SQLException) getCause());
    }
}