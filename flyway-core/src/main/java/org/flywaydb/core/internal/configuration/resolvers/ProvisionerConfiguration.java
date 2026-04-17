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

import java.util.Map;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

public class ProvisionerConfiguration {

    public static void requireDryRunUnsetForProvision(final PropertyResolverContext context) {
        if (context.getConfiguration().getDryRunOutput() != null) {
            throw new FlywayException("Provisioning "
                + context.getEnvironmentName()
                + " would alter the environment or have side effects, so is not supported with dry run enabled.",
                CoreErrorCode.CONFIGURATION);
        }
    }

    public static void requireDryRunUnsetForReprovision(final PropertyResolverContext context) {
        if (context.getConfiguration().getDryRunOutput() != null) {
            throw new FlywayException("Reprovisioning "
                + context.getEnvironmentName()
                + " would alter the environment or have side effects, so is not supported with dry run enabled.",
                CoreErrorCode.CONFIGURATION);
        }
    }

    public static ClassicConfiguration createConfigurationWithEnvironment(final Configuration configuration,
        final String environmentName,
        final EnvironmentModel environmentModel) {

        // Note that doing a regular clone can cause a loop during resolving
        // This is because ClassicConfig will attempt to read the datasource property, which in turn resolves the default
        // environment in order to clone it. As such, we have to clone the underlying configuration model instead.
        final var newConfigurationModel = ConfigurationModel.clone(configuration.getModernConfig());
        final var newConfiguration = new ClassicConfiguration(newConfigurationModel);
        newConfiguration.setAllEnvironments(Map.of(environmentName, environmentModel));
        newConfiguration.setEnvironment(environmentName);
        newConfiguration.setProvisionMode(ProvisionerMode.Skip);
        newConfiguration.setCallbacks(configuration.getCallbacks());
        newConfiguration.setPluginRegister(configuration.getPluginRegister().getCopy());
        newConfiguration.setWorkingDirectory(configuration.getWorkingDirectory());




        return newConfiguration;
    }

    public static ClassicConfiguration createConfigurationWithEnvironment(final PropertyResolverContext context,
        final ResolvedEnvironment resolvedEnvironment) {
        return createConfigurationWithEnvironment(context.getConfiguration(),
            context.getEnvironmentName(),
            resolvedEnvironment.toEnvironmentModel());
    }

    public static ClassicConfiguration createConfigurationWithEnvironment(final PropertyResolverContext context,
        final EnvironmentModel environmentModel) {
        return createConfigurationWithEnvironment(context.getConfiguration(),
            context.getEnvironmentName(),
            environmentModel);
    }
}
