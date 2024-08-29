/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.database.sqlserver;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.List;
import java.util.Properties;

@CustomLog
public class SQLServerDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "SQL Server";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    protected boolean supportsJTDS() {
        return true;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        if (url.startsWith("jdbc-secretsmanager:sqlserver:")) {




            throw new FlywayEditionUpgradeRequiredException(Tier.ENTERPRISE, (Tier) null, "jdbc-secretsmanager");

        }
        return url.startsWith("jdbc:sqlserver:") || (supportsJTDS() && url.startsWith("jdbc:jtds:")) ||
                url.startsWith("jdbc:p6spy:sqlserver:") || (supportsJTDS() && url.startsWith("jdbc:p6spy:jtds:"));
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {





        if (url.startsWith("jdbc:p6spy:sqlserver:") || (supportsJTDS() && url.startsWith("jdbc:p6spy:jtds:"))) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }

        if (url.startsWith("jdbc:jtds:")) {
            if (supportsJTDS()) {
                return "net.sourceforge.jtds.jdbc.Driver";
            } else if (!supportsJTDS()) {
                LOG.warn("JTDS does not support this database. Using the Microsoft JDBC driver instead");
            }
        }

        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Microsoft SQL Server");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new SQLServerDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new SQLServerParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        props.put("applicationName", APPLICATION_NAME);
    }

    @Override
    public void setConfigConnectionProps(Configuration config, Properties props, ClassLoader classLoader) {
        if (config != null) {
            SQLServerConfigurationExtension configurationExtension = config.getPluginRegister().getPlugin(SQLServerConfigurationExtension.class);












            if (StringUtils.hasText(configurationExtension.getKerberos().getLogin().getFile())) {
                throw new FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(config), "sqlserver.kerberos.login.file");
            }
            if (StringUtils.hasText(config.getKerberosConfigFile())) {
                throw new FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(config), "sqlserver.kerberos.config.file");
            }

        }
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        return !(url.contains("integratedSecurity=")
                || url.contains("authentication=ActiveDirectoryIntegrated")
                || url.contains("authentication=ActiveDirectoryMSI"));
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {






        return !(url.contains("integratedSecurity=")
                || url.contains("authentication=ActiveDirectoryIntegrated")
                || url.contains("authentication=ActiveDirectoryMSI"));
    }
}
