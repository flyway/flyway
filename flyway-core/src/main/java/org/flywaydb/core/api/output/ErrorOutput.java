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

import java.util.Collection;
import java.util.List;
import org.flywaydb.core.api.output.errors.ErrorOutputItem;
import org.flywaydb.core.api.output.errors.ExceptionToErrorObjectConverter;
import org.flywaydb.core.api.output.errors.FaultToErrorObjectConverter;
import org.flywaydb.core.api.output.errors.FlywayExceptionToErrorObjectConverter;
import org.flywaydb.core.api.output.errors.FlywayMigrateExceptionToErrorObjectConverter;
import org.flywaydb.core.api.output.errors.FlywaySqlExceptionToErrorObjectConverter;
import org.flywaydb.core.internal.exception.FlywayMigrateException;

public record ErrorOutput(ErrorOutputItem error) implements OperationResult {
    private static final Collection<ExceptionToErrorObjectConverter<? extends Exception, ? extends ErrorOutputItem>> ERROR_OBJECT_CONVERTERS = List.of(
        new FlywayMigrateExceptionToErrorObjectConverter(),
        new FlywaySqlExceptionToErrorObjectConverter(),
        new FlywayExceptionToErrorObjectConverter());

    public static ErrorOutput fromException(final Exception exception) {
        final ExceptionToErrorObjectConverter<? extends Exception, ? extends ErrorOutputItem> converter = ERROR_OBJECT_CONVERTERS.stream()
            .filter(x -> x.getSupportedExceptionType().isInstance(exception))
            .findFirst()
            .orElse(new FaultToErrorObjectConverter());

        return new ErrorOutput(convertException(converter, exception));
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

    private static <E extends Exception, T extends ErrorOutputItem> T convertException(final ExceptionToErrorObjectConverter<E, T> converter,
        final Exception exception) {
        return converter.convert(converter.getSupportedExceptionType().cast(exception));
    }
}
