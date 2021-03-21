/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.authentication.postgres.PgpassFileReader;

import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeMessage;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class PostgreSQLDatabaseType extends DatabaseType {




    @Override
    public String getName() {
        return "PostgreSQL";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        if (url.startsWith("jdbc-secretsmanager:postgresql:")) {




            throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("jdbc-secretsmanager");

        }
        return url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:p6spy:postgresql:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {





        if (url.startsWith("jdbc:p6spy:postgresql:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "org.postgresql.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("PostgreSQL");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new PostgreSQLDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new PostgreSQLParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        props.put("applicationName", APPLICATION_NAME);
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        return !url.contains("user=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {






        // Postgres supports password in URL
        return !url.contains("password=");
    }

    @Override
    public boolean externalAuthPropertiesRequired(String url, String username, String password) {

        return super.externalAuthPropertiesRequired(url, username, password);




    }

    @Override
    public Properties getExternalAuthProperties(String url, String username) {
        PgpassFileReader pgpassFileReader = new PgpassFileReader();

        if (pgpassFileReader.getPgpassFilePath() != null) {
            LOG.info(FlywayTeamsUpgradeMessage.generate(
                    "pgpass file '" + pgpassFileReader.getPgpassFilePath() + "'",
                    "use this for database authentication"));
        }
        return super.getExternalAuthProperties(url, username);







    }
}