/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.core.internal.configuration;

import java.io.File;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

import java.util.Locale;
import org.flywaydb.core.internal.util.ClassUtils;

public class ConfigurationValidator {
    public void validate(Configuration configuration) {
        if (configuration.isBatch() && configuration.getErrorOverrides().length > 0) {
            throw new FlywayException("flyway.batch configuration option is incompatible with flyway.errorOverrides.\n" +
                                              "It is impossible to intercept the errors in a batch process.\n" +
                                              "Set flyway.batch to false, or remove the error overrides.",
                                      CoreErrorCode.CONFIGURATION);
        }

        if (configuration.getDataSource() == null) {
            String errorMessage = "Unable to connect to the database. Configure the url, user and password!";
            if (new File(ClassUtils.getInstallDir(ConfigurationValidator.class) + "/conf/flyway.toml.example").exists()) {
                errorMessage += " Refer to the flyway.toml.example file in the /conf folder in the installation directory.";
            }

            throw new FlywayException(errorMessage, CoreErrorCode.CONFIGURATION);
        }

        for (String key : configuration.getPlaceholders().keySet()) {
            if (key.toLowerCase(Locale.ENGLISH).startsWith("flyway:")) {
                throw new FlywayException("Invalid placeholder ('flyway:' prefix is reserved): " + key);
            }
        }
    }
}