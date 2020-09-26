/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import java.util.List;

public class ValidateResult extends OperationResultBase {

    public String validationError;
    public boolean validationSuccessful;
    public int validateCount;

    public ValidateResult(
            String flywayVersion,
            String database,
            String validationError,
            boolean validationSuccessful,
            int validateCount,
            List<String> warnings) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.validationError = validationError;
        this.validationSuccessful = validationSuccessful;
        this.validateCount = validateCount;
        this.warnings.addAll(warnings);
        this.operation = "validate";
    }

}