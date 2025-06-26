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
package org.flywaydb.core.api.output.errors;

import java.util.Optional;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.output.errors.FaultToErrorObjectConverter.FaultErrorOutputItem;

public class FaultToErrorObjectConverter extends ExceptionToErrorObjectConverter<Exception, FaultErrorOutputItem> {
    public record FaultErrorOutputItem(ErrorCode errorCode,
                                       String message,
                                       String stackTrace,
                                       ErrorCause cause) implements ErrorOutputItem {}

    @Override
    public Class<Exception> getSupportedExceptionType() {
        return Exception.class;
    }

    @Override
    public FaultErrorOutputItem convert(final Exception exception) {
        return new FaultErrorOutputItem(CoreErrorCode.FAULT,
            Optional.ofNullable(exception.getMessage()).orElse("Fault occurred"),
            getStackTrace(exception),
            getCause(exception).orElse(null));
    }
}
