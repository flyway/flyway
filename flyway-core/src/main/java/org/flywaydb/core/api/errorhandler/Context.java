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

import java.util.List;

/**
 * The context passed to an error handler.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 *
 * @deprecated ErrorHandlers have been deprecated and will be removed in Flyway 6.0 use statement-level callbacks instead.
 */
@Deprecated
public interface Context {
    /**
     * @return The warnings that were raised during a migration.
     */
    List<Warning> getWarnings();

    /**
     * @return The errors that were thrown during a migration.
     */
    List<Error> getErrors();
}