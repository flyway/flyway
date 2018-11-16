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
package org.flywaydb.core.api.configuration;

/**
 * Marks a class as configuration aware (executors, resolvers and migrations). Configuration aware classes
 * get the Flyway master configuration injected upon creation. The implementer is responsible for correctly storing
 * the provided {@link FlywayConfiguration} (usually in a field).
 *
 * @deprecated Will be removed in Flyway 6.0.
 */
@Deprecated
public interface ConfigurationAware {
    /**
     * Sets the current configuration. This method should not be called directly, it is called by Flyway itself.
     *
     * @param flywayConfiguration The current Flyway configuration.
     */
    void setFlywayConfiguration(FlywayConfiguration flywayConfiguration);
}