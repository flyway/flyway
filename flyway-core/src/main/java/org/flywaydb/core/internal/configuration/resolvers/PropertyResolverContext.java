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

import java.util.List;

public interface PropertyResolverContext {
    String getWorkingDirectory();
    String getEnvironmentName();
    String resolveValue(String input, ProgressLogger progress);
    String resolvePropertyString(String resolverName, String propertyName, ProgressLogger progress);
    List<String> resolvePropertyStringList(String resolverName, String propertyName, ProgressLogger progress);
    Integer getPropertyInteger(String resolverName, String propertyName);
}