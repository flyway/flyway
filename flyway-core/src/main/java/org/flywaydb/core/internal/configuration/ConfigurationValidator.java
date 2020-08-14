/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;

import java.util.Locale;

public class ConfigurationValidator {
    public void validate(Configuration configuration) {









        if (configuration.getDataSource() == null) {
            throw new FlywayException(
                    "Unable to connect to the database. Configure the url, user and password!",
                    ErrorCode.CONFIGURATION);
        }

        for (String key : configuration.getPlaceholders().keySet()) {
            if (key.toLowerCase(Locale.ENGLISH).startsWith("flyway:")) {
                throw new FlywayException("Invalid placeholder ('flyway:' prefix is reserved): " + key);
            }
        }
    }
}