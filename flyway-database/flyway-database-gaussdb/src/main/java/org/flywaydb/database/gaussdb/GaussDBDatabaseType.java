/*-
 * ========================LICENSE_START=================================
 * flyway-database-gaussdb
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
package org.flywaydb.database.gaussdb;

import static org.flywaydb.core.internal.util.UrlUtils.isAwsWrapperUrl;
import static org.flywaydb.core.internal.util.UrlUtils.isSecretManagerUrl;

import java.util.List;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.authentication.postgres.PgpassFileReader;

import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import lombok.CustomLog;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

/**
 * @author chen zhida
 *
 * Notes: Original code of this class is based on PostgreSQLDatabaseType.
 */
@CustomLog
public class GaussDBDatabaseType extends BaseDatabaseType {

    @Override
    public String getName() {
        return "GaussDB";
    }

    @Override
    public List<String> getSupportedEngines() {
        return List.of(getName());
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return isSecretManagerUrl(url, "gaussdb")
                || url.startsWith("jdbc:gaussdb:")
                || url.startsWith("jdbc:p6spy:gaussdb:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {

        if (url.startsWith("jdbc:p6spy:gaussdb:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }

        return "com.huawei.gaussdb.jdbc.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("GaussDB");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new GaussDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new GaussDBParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        props.put("applicationName", BaseDatabaseType.APPLICATION_NAME);
    }





















    @Override
    public Properties getExternalAuthProperties(String url, String username) {
        PgpassFileReader pgpassFileReader = new PgpassFileReader();

        if (pgpassFileReader.getPgpassFilePath() != null) {
            LOG.info(org.flywaydb.core.internal.license.FlywayUpgradeMessage.generate(
                    "pgpass file '" + pgpassFileReader.getPgpassFilePath() + "'",
                    "use this for database authentication"));
        }
        return super.getExternalAuthProperties(url, username);







    }
}
