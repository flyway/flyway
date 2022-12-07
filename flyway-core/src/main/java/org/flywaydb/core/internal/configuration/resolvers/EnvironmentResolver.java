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
package org.flywaydb.core.internal.configuration.resolvers;

import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentResolver {

    private final Map<String, PropertyResolver> propertyResolvers;

    public EnvironmentResolver(Map<String, PropertyResolver> propertyResolvers) {
        this.propertyResolvers = propertyResolvers;
    }

    public ResolvedEnvironment resolve(EnvironmentModel environment) {
        Map<String, Map<String, String>> resolversToConfigure = environment.getResolvers();

        PropertyResolverContext context = new PropertyResolverContextImpl(propertyResolvers, resolversToConfigure);
        ResolvedEnvironment result = new ResolvedEnvironment();
        result.setDriver(environment.getDriver());
        result.setConnectRetries(environment.getConnectRetries());
        result.setConnectRetriesInterval(environment.getConnectRetriesInterval());
        result.setInitSql(environment.getInitSql());
        result.setSchemas(environment.getSchemas());

        if (environment.getJdbcProperties() != null) {
            Map<String, String> jdbcResolvedProps = new HashMap<>();
            for(Map.Entry<String, String> entry : environment.getJdbcProperties().entrySet()) {
                jdbcResolvedProps.put(entry.getKey(), context.resolveValue(entry.getValue()));
            }
            result.setJdbcProperties(jdbcResolvedProps);
        }


        result.setSchemas(environment.getSchemas());

        result.setPassword(context.resolveValue(environment.getPassword()));
        result.setUser(context.resolveValue(environment.getUser()));
        result.setUrl(context.resolveValue(environment.getUrl()));
        result.setToken(context.resolveValue(environment.getToken()));

        return result;
    }
}