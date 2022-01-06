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
package org.flywaydb.core.api.exception;

import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

/**
 * Exception thrown when Flyway encounters a problem with Validate.
 */
public class FlywayValidateException extends FlywayException {

    public FlywayValidateException(ErrorDetails errorDetails, String allValidateMessages) {
        super("Validate failed: " + errorDetails.errorMessage + "\n" + allValidateMessages +
                      "\nNeed more flexibility with validation rules? Learn more: " + FlywayDbWebsiteLinks.CUSTOM_VALIDATE_RULES, errorDetails.errorCode);
    }

}