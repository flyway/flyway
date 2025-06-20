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
package org.flywaydb.core.api.output;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.exception.FlywayMigrateException;
import org.flywaydb.core.internal.exception.FlywaySqlException;

public class ErrorOutput implements OperationResult {

    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class ErrorOutputItem {
        public ErrorCode errorCode;
        public String sqlState;
        public Integer sqlErrorCode;
        public String message;
        public String stackTrace;
        public Integer lineNumber;
        public String path;
        public ErrorCause cause;
    }

    public record ErrorCause(String message, String stackTrace, ErrorCause cause) {}

    public ErrorOutputItem error;

    public ErrorOutput(final ErrorCode errorCode,
        final String message,
        final String stackTrace,
        final Integer lineNumber,
        final String path,
        final ErrorCause cause) {
        this.error = new ErrorOutputItem(errorCode, null, null, message, stackTrace, lineNumber, path, cause);
    }

    public ErrorOutput(final ErrorCode errorCode,
        final String sqlState,
        final Integer sqlErrorCode,
        final String message,
        final String stackTrace,
        final Integer lineNumber,
        final String path,
        final ErrorCause cause) {
        this.error = new ErrorOutputItem(errorCode,
            sqlState,
            sqlErrorCode,
            message,
            stackTrace,
            lineNumber,
            path,
            cause);
    }

    public static ErrorOutput fromException(final Exception exception) {
        final String message = exception.getMessage();

        if (exception instanceof final FlywayMigrateException flywayMigrateException
            && flywayMigrateException.getAbsolutePathOnDisk() != null) {

            return new ErrorOutput(flywayMigrateException.getMigrationErrorCode(),
                flywayMigrateException.getSqlState(),
                flywayMigrateException.getSqlErrorCode(),
                message == null ? "Error occurred" : message,
                null,
                flywayMigrateException.getLineNumber(),
                flywayMigrateException.getAbsolutePathOnDisk(),
                getCause(exception).orElse(null));
        }

        if (exception instanceof final FlywaySqlException flywaySqlException) {
            return new ErrorOutput(flywaySqlException.getErrorCode(),
                flywaySqlException.getSqlState(),
                flywaySqlException.getSqlErrorCode(),
                message == null ? "Error occurred" : message,
                null,
                null,
                null,
                getCause(exception).orElse(null));
        }

        if (exception instanceof final FlywayException flywayException) {
            return new ErrorOutput(flywayException.getErrorCode(),
                null,
                null,
                message == null ? "Error occurred" : message,
                null,
                null,
                null,
                getCause(exception).orElse(null));
        }

        return new ErrorOutput(CoreErrorCode.FAULT,
            null,
            null,
            message == null ? "Fault occurred" : message,
            getStackTrace(exception),
            null,
            null,
            getCause(exception).orElse(null));
    }

    public static MigrateErrorResult fromMigrateException(final FlywayMigrateException exception) {
        return exception.getErrorResult();
    }

    public static OperationResult toOperationResult(final Exception exception) {
        if (exception instanceof FlywayMigrateException) {
            return fromMigrateException((FlywayMigrateException) exception);
        } else {
            return fromException(exception);
        }
    }

    private static String getStackTrace(final Throwable exception) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintStream printStream;

        printStream = new PrintStream(output, true, StandardCharsets.UTF_8);

        exception.printStackTrace(printStream);

        return output.toString(StandardCharsets.UTF_8);
    }

    private static Optional<ErrorCause> getCause(final Throwable e) {
        return Optional.ofNullable(e.getCause())
            .map(cause -> new ErrorCause(cause.getMessage(), getStackTrace(cause), getCause(cause).orElse(null)));
    }
}
