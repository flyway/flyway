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
package org.flywaydb.database.mysql.mariadb;

import static org.flywaydb.core.internal.util.UrlUtils.isSecretManagerUrl;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class MariaDBDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "MariaDB";
    }

    @Override
    public int getPriority() {
        // Maria needs to be checked in advance of MySql
        return 1;
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(final String url) {
        return isSecretManagerUrl(url, "mariadb") || url.startsWith("jdbc:mariadb:") || url.startsWith(
            "jdbc:p6spy:mariadb:");
    }

    @Override
    public String getDriverClass(final String url, final ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:mariadb:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(final String databaseProductName,
        final String databaseProductVersion,
        final Connection connection) {
        return databaseProductName.startsWith("MariaDB")
            // Older versions of the driver report MariaDB as "MySQL"
            || (databaseProductName.contains("MySQL") && databaseProductVersion.contains("MariaDB"))
            // Azure Database For MariaDB reports as "MySQL"
            || (databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("MariaDB"));
    }

    @Override
    public Database createDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        return new MariaDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(final Configuration configuration,
        final ResourceProvider resourceProvider,
        final ParsingContext parsingContext) {
        return new MariaDBParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(final String url, final Properties props, final ClassLoader classLoader) {
        props.put("connectionAttributes", "program_name:" + APPLICATION_NAME);
    }
}
