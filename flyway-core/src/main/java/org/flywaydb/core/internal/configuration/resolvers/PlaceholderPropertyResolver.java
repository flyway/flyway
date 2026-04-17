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

import java.util.Optional;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;

public class PlaceholderPropertyResolver implements PropertyResolver {

    @Override
    public String getName() {
        return "placeholder";
    }

    @Override
    public String resolve(final String key, final PropertyResolverContext context, final ProgressLogger progress) {
        if ("flyway".equals(key)) {
            throw new FlywayException(
                "Only user defined placeholders are supported, but detected attempt to use a default placeholder.",
                CoreErrorCode.CONFIGURATION);
        }
        return Optional.ofNullable(context.getConfiguration().getPlaceholders()).map(placeholders -> placeholders.get(
            key)).orElseThrow(() -> new FlywayException("Unable to resolve placeholder: '" + key + "'",
            CoreErrorCode.CONFIGURATION));
    }

    @Override
    public Class<?> getConfigClass() {
        return null;
    }
}
