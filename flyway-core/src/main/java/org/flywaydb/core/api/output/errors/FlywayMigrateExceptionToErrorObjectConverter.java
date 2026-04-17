/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.api.output.errors;

import java.util.Optional;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.output.errors.FlywayMigrateExceptionToErrorObjectConverter.FlywayMigrateErrorOutputItem;
import org.flywaydb.core.internal.exception.FlywayMigrateException;

public class FlywayMigrateExceptionToErrorObjectConverter extends ExceptionToErrorObjectConverter<FlywayMigrateException, FlywayMigrateErrorOutputItem> {
    public record FlywayMigrateErrorOutputItem(ErrorCode errorCode,
                                               String sqlState,
                                               Integer sqlErrorCode,
                                               String message,
                                               Integer lineNumber,
                                               String path,
                                               ErrorCause cause) implements ErrorOutputItem {}

    @Override
    public Class<FlywayMigrateException> getSupportedExceptionType() {
        return FlywayMigrateException.class;
    }

    @Override
    public FlywayMigrateErrorOutputItem convert(final FlywayMigrateException exception) {
        return new FlywayMigrateErrorOutputItem(exception.getMigrationErrorCode(),
            exception.getSqlState(),
            exception.getSqlErrorCode(),
            Optional.ofNullable(exception.getMessage()).orElse("Error occurred"),
            exception.getAbsolutePathOnDisk() != null ? exception.getLineNumber() : null,
            exception.getAbsolutePathOnDisk(),
            getCause(exception).orElse(null));
    }
}
