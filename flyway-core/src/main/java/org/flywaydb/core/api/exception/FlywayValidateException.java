/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.api.exception;

import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;

/**
 * Exception thrown when Flyway encounters a problem with Validate.
 */
public class FlywayValidateException extends FlywayException {

    public FlywayValidateException(ErrorDetails errorDetails) {
        super("Validate failed: " + errorDetails.errorMessage, errorDetails.errorCode);
    }

}