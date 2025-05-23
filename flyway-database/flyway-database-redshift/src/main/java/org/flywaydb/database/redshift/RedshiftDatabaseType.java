/*-
 * ========================LICENSE_START=================================
 * flyway-database-redshift
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
package org.flywaydb.database.redshift;

import static org.flywaydb.core.internal.util.UrlUtils.isSecretManagerUrl;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.ClassUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

public class RedshiftDatabaseType extends BaseDatabaseType {
    private static final String REDSHIFT_JDBC4_DRIVER = "com.amazon.redshift.jdbc4.Driver";
    private static final String REDSHIFT_JDBC41_DRIVER = "com.amazon.redshift.jdbc41.Driver";

    @Override
    public String getName() {
        return "Redshift";
    }

    @Override
    public int getPriority() {
        // Redshift needs to be checked in advance of Postgres
        return 1;
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return isSecretManagerUrl(url, "redshift") || url.startsWith("jdbc:redshift:") || url.startsWith("jdbc:p6spy:redshift:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {





        if (url.startsWith("jdbc:p6spy:redshift:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "com.amazon.redshift.jdbc42.Driver";
    }

    @Override
    public String getBackupDriverClass(String url, ClassLoader classLoader) {
        if (ClassUtils.isPresent(REDSHIFT_JDBC41_DRIVER, classLoader)) {
            return REDSHIFT_JDBC41_DRIVER;
        }
        return REDSHIFT_JDBC4_DRIVER;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        if (databaseProductName.startsWith("PostgreSQL")) {
            if (databaseProductName.startsWith("PostgreSQL 8") && BaseDatabaseType.getSelectVersionOutput(connection).contains("Redshift")) {
                return true;
            }
        }
        // This is the open-source driver at https://github.com/aws/amazon-redshift-jdbc-driver
        if (databaseProductName.startsWith("Redshift")) {
            return true;
        }
        return false;
    }

    @Override
    public void setOverridingConnectionProps(Map<String, String> props) {
        // Necessary because the Amazon v2 driver does not appear to respect the way Properties.get() handles defaults.
        // If not forced to false, the driver allows resultsets to be read on different threads and will throw if
        // connections are closed before all results are read.
        props.put("enableFetchRingBuffer", "false");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new RedshiftDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new RedshiftParser(configuration, parsingContext);
    }
}
