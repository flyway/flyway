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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.configuration.ObsoleteParameter;
import org.flywaydb.core.api.exception.ObsoleteConfigurationParametersException;
import org.flywaydb.core.api.output.errors.ObsoleteConfigurationParametersExceptionToErrorObjectConverter.ObsoleteConfigurationParametersErrorOutputItem;

public class ObsoleteConfigurationParametersExceptionToErrorObjectConverter extends ExceptionToErrorObjectConverter<ObsoleteConfigurationParametersException, ObsoleteConfigurationParametersErrorOutputItem> {

    public record ObsoleteConfigurationParametersErrorOutputItem(ErrorCode errorCode,
                                                                 String message,
                                                                 ErrorCause cause,
                                                                 Collection<ObsoleteParameter> obsoleteParameters) implements
                                                                                                                   ErrorOutputItem {
        public ObsoleteConfigurationParametersErrorOutputItem {
            obsoleteParameters = List.copyOf(obsoleteParameters);
        }
    }

    @Override
    public Class<ObsoleteConfigurationParametersException> getSupportedExceptionType() {
        return ObsoleteConfigurationParametersException.class;
    }

    @Override
    public ObsoleteConfigurationParametersErrorOutputItem convert(final ObsoleteConfigurationParametersException exception) {
        return new ObsoleteConfigurationParametersErrorOutputItem(exception.getErrorCode(),
            Optional.ofNullable(exception.getMessage()).orElse("Error occurred"),
            getCause(exception).orElse(null),
            exception.getObsoleteParameters());
    }
}
