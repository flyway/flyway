/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.database.mysql.mariadb;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.database.mysql.MySQLDatabase;
import org.flywaydb.core.internal.database.mysql.MySQLParser;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class MariaDBDatabaseType extends DatabaseType {
    @Override
    public String getName() {
        return "MariaDB";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        if (url.startsWith("jdbc-secretsmanager:mariadb:")) {




            throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("jdbc-secretsmanager");

        }

        return url.startsWith("jdbc:mariadb:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {






        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("MariaDB")
                // Older versions of the driver report MariaDB as "MySQL"
                || (databaseProductName.contains("MySQL") && databaseProductVersion.contains("MariaDB"))
                // Azure Database For MariaDB reports as "MySQL"
                || (databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("MariaDB"));
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new MariaDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new MySQLParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        props.put("connectionAttributes", "program_name:" + APPLICATION_NAME);
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {






        return super.detectPasswordRequiredByUrl(url);
    }
}