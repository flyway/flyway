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
package org.flywaydb.core.internal.configuration.resolvers;

import java.util.Collection;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.configuration.Configuration;

import org.flywaydb.core.extensibility.ConfigurationExtension;

public interface PropertyResolverContext {
    Configuration getConfiguration();

    default FlywayTelemetryManager getTelemetryManager() {
        return null;
    }

    String getWorkingDirectory();

    String getEnvironmentName();

    String resolveValue(String input, ProgressLogger progress);

    String resolveValueOrThrow(String input, ProgressLogger progress, String propertyName);

    Collection<String> resolveValues(Collection<String> input, ProgressLogger progress);

    ConfigurationExtension getResolverConfiguration(String resolverName);

    ConfigurationExtension getResolverConfigurationOrThrow(String resolverName);
}
