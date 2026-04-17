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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.flywaydb.core.extensibility.Plugin;

public abstract class ExceptionToErrorObjectConverter<E extends Exception, T extends ErrorOutputItem> implements Plugin {
    public abstract Class<E> getSupportedExceptionType();

    public abstract T convert(E exception);

    String getStackTrace(final Throwable exception) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintStream printStream;

        printStream = new PrintStream(output, true, StandardCharsets.UTF_8);

        exception.printStackTrace(printStream);

        return output.toString(StandardCharsets.UTF_8);
    }

    protected Optional<ErrorCause> getCause(final Throwable e) {
        return Optional.ofNullable(e.getCause())
            .map(cause -> new ErrorCause(cause.getMessage(), getStackTrace(cause), getCause(cause).orElse(null)));
    }
}
