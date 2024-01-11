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