/*-
 * ========================LICENSE_START=================================
 * flyway-database-oracle
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

/**
 * Configuration extension for MySQL database-specific settings.
 * <p>
 * This extension provides additional configuration options specific to MySQL databases,
 * allowing users to customize Flyway's behavior when working with MySQL.
 * </p>
 */
@Data
public class MysqlConfigurationExtension implements ConfigurationExtension {

    /**
     * Configuration parameter name for skipping user variable reset.
     */
    private static final String FLYWAY_MYSQL_SKIP_USER_VARIABLE_RESET = "flyway.mysql.skipUserVariableReset";

    /**
     * Skip the user variable reset capability check during MySQL connection initialization. 
     * If your MySQL user account does not have permission to access `performance_schema`, you can enable this setting to skip the check. This will prevent `SQLSyntaxErrorException` errors during Flyway initialization.
     * when using connection pools like Druid, which treat this error as fatal and disable the affected connection, potentially triggering cascading failures in dependent components. 
     * Default value is {@code false}.
     * </p>
     */
    private Boolean skipUserVariableReset = false;

    @Override
    public String getNamespace() {
        return "mysql";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        switch (environmentVariable) {
            case "FLYWAY_MYSQL_SKIP_USER_VARIABLE_RESET":
                return FLYWAY_MYSQL_SKIP_USER_VARIABLE_RESET;
            default:
                return null;
        }
    }
}
