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
package org.flywaydb.core.internal.exception;

import java.sql.SQLException;
import lombok.Getter;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * The specific exception thrown when Flyway encounters a problem in SQL.
 */
@Getter
public class FlywaySqlException extends FlywayException {
    private final String sqlState;
    private final int sqlErrorCode;

    public FlywaySqlException(final String message, final SQLException sqlException) {
        super(message, sqlException, CoreErrorCode.DB_CONNECTION);
        sqlState = sqlException.getSQLState();
        sqlErrorCode = sqlException.getErrorCode();
    }

    @Override
    public String getMessage() {
        final String title = super.getMessage();
        final String underline = StringUtils.trimOrPad("", title.length(), '-');

        return title + "\n" + underline + "\n" + ExceptionUtils.toMessage((SQLException) getCause());
    }
}
