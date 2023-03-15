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
package org.flywaydb.telemetry.otel.exceptions;

import org.flywaydb.core.api.FlywayException;

public class FlywaySanitizedException extends FlywayException {

    public FlywaySanitizedException(Throwable cause) {
        super(cause.getClass().getName());
        this.setStackTrace(cause.getStackTrace());
    }
}