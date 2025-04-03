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
package org.flywaydb.database.oracle;

import lombok.Data;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Data
public class OracleConfigurationExtension implements ConfigurationExtension {

    private static final String ORACLE_SQLPLUS = "flyway.oracle.sqlplus";
    private static final String ORACLE_SQLPLUS_WARN = "flyway.oracle.sqlplusWarn";
    private static final String ORACLE_KERBEROS_CACHE_FILE = "flyway.oracle.kerberosCacheFile";
    private static final String ORACLE_WALLET_LOCATION = "flyway.oracle.walletLocation";

    private Boolean sqlplus = false;
    private Boolean sqlplusWarn = false;
    private String kerberosCacheFile;
    private String walletLocation;


    @Override
    public String getNamespace() {
        return "oracle";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        switch (environmentVariable) {
            case "FLYWAY_ORACLE_KERBEROS_CACHE_FILE":
                return ORACLE_KERBEROS_CACHE_FILE;
            case "FLYWAY_ORACLE_SQLPLUS":
                return ORACLE_SQLPLUS;
            case "FLYWAY_ORACLE_SQLPLUS_WARN":
                return ORACLE_SQLPLUS_WARN;
            case "FLYWAY_ORACLE_WALLET_LOCATION":
                return ORACLE_WALLET_LOCATION;
            default:
                return null;
        }
    }
}
