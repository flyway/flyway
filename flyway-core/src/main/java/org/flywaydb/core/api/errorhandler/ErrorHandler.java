/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api.errorhandler;

/**
 * Handler for warnings and errors that occur during a migration. This can be used to customize Flyway's behavior by for example
 * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 *
 * @deprecated ErrorHandlers have been deprecated and will be removed in Flyway 6.0 use statement-level callbacks instead.
 */
@Deprecated
public interface ErrorHandler {
    /**
     * Handles warnings and errors that occurred during a migration.
     *
     * @param context The context.
     * @return {@code true} if they were handled, {@code false} if they weren't and Flyway should fall back to its default handling.
     */
    boolean handle(Context context);
}