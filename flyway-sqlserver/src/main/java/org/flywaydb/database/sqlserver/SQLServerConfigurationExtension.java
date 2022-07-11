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
package org.flywaydb.database.sqlserver;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.FLYWAY_PLUGINS_PREFIX;

@Getter
@Setter
@CustomLog
public class SQLServerConfigurationExtension implements ConfigurationExtension {
    private static final String SQLSERVER_KERBEROS_LOGIN_FILE = "sqlserver.kerberos.login.file";
    private static final String KERBEROS_LOGIN_FILE = FLYWAY_PLUGINS_PREFIX + SQLSERVER_KERBEROS_LOGIN_FILE;

    private String kerberosLoginFile;

    @Override
    public void extractParametersFromConfiguration(Map<String, String> configuration) {
        kerberosLoginFile = getProperty(configuration, KERBEROS_LOGIN_FILE, kerberosLoginFile);
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        switch (environmentVariable) {
            case "FLYWAY_PLUGINS_SQL_SERVER_KERBEROS_LOGIN_FILE":
                return KERBEROS_LOGIN_FILE;
            default:
                return null;
        }
    }

    private String getProperty(Map<String, String> configuration, String key, String fallback) {

         if (org.flywaydb.core.internal.util.StringUtils.hasText(fallback)) {
           throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException(SQLSERVER_KERBEROS_LOGIN_FILE);
         }
         return null;






    }
}