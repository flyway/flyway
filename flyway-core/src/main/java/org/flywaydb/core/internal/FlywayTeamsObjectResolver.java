/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.ClassUtils;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlywayTeamsObjectResolver {

    public static <T> T resolve(Class<T> clazz, Object... params) {
        String packageName = clazz.getPackage().getName();
        String className = clazz.getSimpleName();

        // Using instance class loader rather than static because it's more reliable
        // https://github.com/flyway/flyway/issues/3177
        @SuppressWarnings({"InstantiationOfUtilityClass", "InstantiatingObjectToGetClassObject"})
        ClassLoader classLoader = new FlywayTeamsObjectResolver().getClass().getClassLoader();

        if (VersionPrinter.EDITION != Edition.COMMUNITY) {
            String teamsPath = packageName + ".teams." + className;
            try {
                return ClassUtils.instantiate(teamsPath, classLoader, params);
            } catch (FlywayException e) {
                LOG.debug(e.getMessage() + ". Defaulting to Community Edition for " + className);
            }
        }

        return ClassUtils.instantiate(packageName + "." + className, classLoader, params);
    }
}