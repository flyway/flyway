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
package org.flywaydb.core.internal.database.bigquery;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.ClassUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

public class BigQueryDatabaseType extends DatabaseType {
    private static final String BIGQUERY_JDBC42_DRIVER = "com.simba.googlebigquery.jdbc42.Driver";
    private static final String BIGQUERY_JDBC_DRIVER = "com.simba.googlebigquery.jdbc.Driver";

    @Override
    public String getName() {
        return "BigQuery";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:bigquery:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        return BIGQUERY_JDBC42_DRIVER;
    }

    @Override
    public String getBackupDriverClass(String url, ClassLoader classLoader) {
        if (ClassUtils.isPresent(BIGQUERY_JDBC_DRIVER, classLoader)) {
            return BIGQUERY_JDBC_DRIVER;
        }
        return BIGQUERY_JDBC_DRIVER;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        // https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers
        System.out.println("DEBUGGY " + databaseProductName + " " + databaseProductVersion);
        return databaseProductName.toLowerCase().startsWith("bigquery");
    }

    @Override
    public void setOverridingConnectionProps(Map<String, String> props) {}

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new BigQueryDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new BigQueryParser(configuration, parsingContext);
    }
}