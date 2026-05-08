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
package org.flywaydb.core.api.exception;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ObsoleteParameter;

/**
 * Thrown when configuration parameters are no longer supported but the configuration is otherwise recoverable. Carries
 * the list of obsolete parameters so machine-readable consumers can react (e.g. surface them in UI).
 */
@Getter
public class ObsoleteConfigurationParametersException extends FlywayException {
    private final Collection<ObsoleteParameter> obsoleteParameters;

    public ObsoleteConfigurationParametersException(final String message,
        final Collection<ObsoleteParameter> obsoleteParameters) {
        super(message, CoreErrorCode.CONFIGURATION_RECOVERABLE);
        this.obsoleteParameters = List.copyOf(obsoleteParameters);
    }
}
