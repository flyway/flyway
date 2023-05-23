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

import org.flywaydb.core.api.FlywayException;

import java.util.Map;

public class PropertyResolverContextImpl implements PropertyResolverContext{

    private final Map<String, PropertyResolver> resolvers;
    private final Map<String, Map<String, String>> resolverProperties;

    @Override
    public String resolveValue(String value) {
        if (value == null || !value.startsWith("$")) {
            return value;
        } else if (value.startsWith("$$")) {
            return value.substring(1);
        }

        int splitIndex = value.indexOf(".");
        String resolverName = splitIndex != -1 ? value.substring(1, splitIndex) : value.substring(1);
        String resolverParam = splitIndex != -1 ? value.substring(splitIndex + 1) : null;

        if (!resolvers.containsKey(resolverName)) {
            throw new FlywayException("Unknown resolver: " + resolverName);
        }
        return resolvers.get(resolverName).resolve(resolverParam, this);

    }
    
    public String resolveProperty(String resolverName, String propertyName) {
        if (resolverProperties == null) {
            return null;
        }

        Map<String, String> properties = resolverProperties.get(resolverName);
        if (properties == null) {
            return null;
        }

        return resolveValue(properties.get(propertyName));
    }

    public PropertyResolverContextImpl(Map<String, PropertyResolver> resolvers, Map<String, Map<String, String>> resolverProperties) {
        this.resolvers = resolvers;
        this.resolverProperties = resolverProperties;
    }
}