/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.FlywayTeamsObjectResolver;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.Collections;
import java.util.Properties;

@CustomLog
@ExtensionMethod(FlywayTeamsObjectResolver.class)
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




            throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("jdbc-secretsmanager");

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
        return SQLServerDatabase.class.resolve(configuration, jdbcConnectionFactory, statementInterceptor);
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