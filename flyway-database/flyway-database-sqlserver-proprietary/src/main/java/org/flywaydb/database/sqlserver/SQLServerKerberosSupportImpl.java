package org.flywaydb.database.sqlserver;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.util.StringUtils;

public class SQLServerKerberosSupportImpl implements SQLServerKerberosSupport {
    @Override
    public void configureKerberos(final Configuration config,
        final SQLServerConfigurationExtension configurationExtension) {
        final String loginFile = configurationExtension.getKerberos().getLogin().getFile();
        if (StringUtils.hasText(loginFile)) {
            LicenseGuard.guard(config, Tier.PREMIUM, "sqlserver.kerberos.login.file");
            System.setProperty("java.security.auth.login.config", loginFile);
        }

        final String kerberosConfigFile = config.getKerberosConfigFile();
        if (StringUtils.hasText(kerberosConfigFile)) {
            LicenseGuard.guard(config, Tier.PREMIUM, "sqlserver.kerberos.config.file");
            System.setProperty("java.security.krb5.conf", kerberosConfigFile);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }
}