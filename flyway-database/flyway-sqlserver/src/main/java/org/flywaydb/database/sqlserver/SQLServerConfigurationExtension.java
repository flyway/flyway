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
package org.flywaydb.database.sqlserver;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.command.clean.CleanModel;

import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.FLYWAY_PLUGINS_PREFIX;

@Getter
@Setter
@CustomLog
public class SQLServerConfigurationExtension implements ConfigurationExtension {
    static final String SQLSERVER_KERBEROS_LOGIN_FILE = "sqlserver.kerberos.login.file";
    static final String KERBEROS_LOGIN_FILE = "flyway." + SQLSERVER_KERBEROS_LOGIN_FILE;

    static final String CLEAN_MODE = "flyway.sqlserver.clean.mode";
    static final String CLEAN_SCHEMAS_EXCLUDE = "flyway.sqlserver.clean.schemas.exclude";

    @Getter
    @Setter
    private KerberosModel kerberos = new KerberosModel();

    @Getter
    @Setter
    private CleanModel clean;

    @Override
    public String getNamespace() {
        return "sqlserver";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        switch (environmentVariable) {
            case "FLYWAY_PLUGINS_SQL_SERVER_KERBEROS_LOGIN_FILE":
            case "FLYWAY_SQL_SERVER_KERBEROS_LOGIN_FILE":
                return KERBEROS_LOGIN_FILE;
            case "FLYWAY_PLUGINS_CLEAN_MODE":
            case "FLYWAY_SQL_SERVER_CLEAN_MODE":
                return CLEAN_MODE;
            case "FLYWAY_PLUGINS_CLEAN_SCHEMAS_EXCLUDE":
            case "FLYWAY_SQL_SERVER_CLEAN_SCHEMAS_EXCLUDE":
                return CLEAN_SCHEMAS_EXCLUDE;
            default:
                return null;
        }
    }
}