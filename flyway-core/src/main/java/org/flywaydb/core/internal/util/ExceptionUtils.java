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
package org.flywaydb.core.internal.util;

import java.sql.SQLException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {
    public static final String CONTACT_EMAIL = "DatabaseDevOps@red-gate.com";
    /**
     * @return The root cause or the throwable itself if it doesn't have a cause.
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = throwable;
        Throwable rootCause;
        while ((rootCause = cause.getCause()) != null) {
            cause = rootCause;
        }

        return cause;
    }

    /**
     * Retrieves the exact location where this exception was thrown.
     */
    public static String getThrowLocation(Throwable e) {
        StackTraceElement element = e.getStackTrace()[0];
        int lineNumber = element.getLineNumber();
        return element.getClassName() + "." + element.getMethodName()
                + (lineNumber < 0 ? "" : ":" + lineNumber)
                + (element.isNativeMethod() ? " [native]" : "");
    }

    /**
     * Transforms the details of this SQLException into a nice readable message.
     */
    public static String toMessage(SQLException e) {
        SQLException cause = e;
        while (cause.getNextException() != null) {
            cause = cause.getNextException();
        }

        String message = "SQL State  : " + cause.getSQLState() + "\n"
                + "Error Code : " + cause.getErrorCode() + "\n";
        if (cause.getMessage() != null) {
            message += "Message    : " + cause.getMessage().trim() + "\n";
        }

        return message;

    }

    /**
     * Retrieves the message of the Flyway Exception that's nested in.
     */
    public static Optional<String> getFlywayExceptionMessage(final Throwable throwable) {
        var exception = throwable.getCause();
        while (exception != null) {
            if (exception instanceof FlywayException) {
                return Optional.of(exception.getMessage());
            }
            exception = exception.getCause();
        }
        return Optional.empty();
    }

    public static boolean exceptionHasCauseOf(final Throwable exception, final Class<?> cause) {
        Throwable throwable = exception;
        while (throwable != null) {
            if (cause.isInstance(throwable)) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
}
