/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentResolver {

    private final Map<String, PropertyResolver> propertyResolvers;
    private final Map<String, EnvironmentProvisioner> environmentProvisioners;

    public EnvironmentResolver(Map<String, PropertyResolver> propertyResolvers, Map<String, EnvironmentProvisioner> environmentProvisioners) {
        this.propertyResolvers = propertyResolvers;
        this.environmentProvisioners = environmentProvisioners;
    }

    public ResolvedEnvironment resolve(String environmentName, EnvironmentModel environment, Configuration configuration, ProgressLogger progress) {
        return resolve(environmentName, environment, ProvisionerMode.Provision, configuration, progress);
    }

    public ResolvedEnvironment resolve(String environmentName, EnvironmentModel environment, ProvisionerMode mode, Configuration configuration, ProgressLogger progress) {
        Map<String, Map<String, Object>> resolversToConfigure = environment.getResolvers();

        PropertyResolverContext context = new PropertyResolverContextImpl(environmentName, configuration, propertyResolvers, resolversToConfigure);
        ResolvedEnvironment result = new ResolvedEnvironment();
        result.setDriver(environment.getDriver());
        result.setConnectRetries(environment.getConnectRetries());
        result.setConnectRetriesInterval(environment.getConnectRetriesInterval());
        result.setInitSql(environment.getInitSql());
        result.setSchemas(environment.getSchemas());
        result.setJarDirs(environment.getJarDirs());

        progress.pushSteps(2);
        ProgressLogger provisionProgress = progress.subTask("provision");
        ProgressLogger resolveProgress = progress.subTask("resolve");

        EnvironmentProvisioner provisioner = getProvisioner(environment.getProvisioner(), context, provisionProgress);
        if (mode == ProvisionerMode.Provision) {
            progress.log("Provisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.preProvision(context, provisionProgress);
        } else if (mode == ProvisionerMode.Reprovision) {
            progress.log("Reprovisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.preReprovision(context, provisionProgress);
        }

        progress.log("Resolving environment properties " + environmentName);
        if (environment.getJdbcProperties() != null) {
            Map<String, String> jdbcResolvedProps = new HashMap<>();
            for (Map.Entry<String, String> entry : environment.getJdbcProperties().entrySet()) {
                jdbcResolvedProps.put(entry.getKey(), context.resolveValue(entry.getValue(), resolveProgress));
            }
            result.setJdbcProperties(jdbcResolvedProps);
        }

        result.setPassword(context.resolveValue(environment.getPassword(), resolveProgress));
        result.setUser(context.resolveValue(environment.getUser(), resolveProgress));
        result.setUrl(context.resolveValue(environment.getUrl(), resolveProgress));
        result.setToken(context.resolveValue(environment.getToken(), resolveProgress));

        if (mode == ProvisionerMode.Provision) {
            progress.log("Provisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.postProvision(context, result, provisionProgress);
        } else if (mode == ProvisionerMode.Reprovision) {
            progress.log("Reprovisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.postReprovision(context, result, provisionProgress);
        }

        return result;
    }

    private EnvironmentProvisioner getProvisioner(String provisionerName, PropertyResolverContext context, ProgressLogger progress) {
        String name = context.resolveValue(provisionerName, progress);
        if (name != null) {
            if (!environmentProvisioners.containsKey(provisionerName)) {
                throw new FlywayException("Unknown provisioner '" + provisionerName + "' for environment " + context.getEnvironmentName(), ErrorCode.CONFIGURATION);
            }
            return environmentProvisioners.get(provisionerName);
        }
        return new EnvironmentProvisionerNone();
    }
}