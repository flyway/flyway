/*-
 * ========================LICENSE_START=================================
 * flyway-mysql
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
package org.flywaydb.database.mysql;

import lombok.Data;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Data
public class MySQLConfigurationExtension implements ConfigurationExtension {
    private static final String NAMED_LOCK_ON_WSREP = "flyway.mysql.namedLockOnWsrep";

    private Boolean namedLockOnWsrep = false;

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(final String environmentVariable) {
        if ("FLYWAY_MYSQL_NAMED_LOCK_ON_WSREP".equals(environmentVariable)) {
            return NAMED_LOCK_ON_WSREP;
        }
        return null;
    }

    @Override
    public String getNamespace() {
        return "mysql";
    }
}
