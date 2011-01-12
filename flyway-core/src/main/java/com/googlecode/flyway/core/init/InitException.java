/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.init;

import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.SchemaVersion;

/**
 * Exception indicating that migration failed.
 */
public class InitException extends FlywayException {
    /**
     * Creates a new InitException with this error message and this cause.
     *
     * @param message The error message.
     * @param cause   The exception that caused this.
     */
    public InitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new InitException with this error message.
     *
     * @param message The error message.
     */
    public InitException(String message) {
        super(message);
    }
}
