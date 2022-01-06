/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.api.output;

import org.flywaydb.core.api.ErrorDetails;

import java.util.List;
import java.util.stream.Collectors;

public class ValidateResult extends OperationResultBase {
    public final ErrorDetails errorDetails;
    public final List<ValidateOutput> invalidMigrations;
    public final boolean validationSuccessful;
    public final int validateCount;

    public ValidateResult(
            String flywayVersion,
            String database,
            ErrorDetails errorDetails,
            boolean validationSuccessful,
            int validateCount,
            List<ValidateOutput> invalidMigrations,
            List<String> warnings) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.errorDetails = errorDetails;
        this.validationSuccessful = validationSuccessful;
        this.validateCount = validateCount;
        this.invalidMigrations = invalidMigrations;
        this.warnings.addAll(warnings);
        this.operation = "validate";
    }

    public String getAllErrorMessages() {
        return invalidMigrations.stream().map(m -> m.errorDetails.errorMessage).collect(Collectors.joining("\n"));
    }
}