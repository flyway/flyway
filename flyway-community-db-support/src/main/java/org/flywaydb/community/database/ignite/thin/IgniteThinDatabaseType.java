/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.ignite.thin;

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

/*
 * # Apache Ignite Thin: jdbc:ignite:thin://<hostAndPortRange0>[,<hostAndPortRange1>]...[,<hostAndPortRangeN>][/schema][?<params>] where hostAndPortRange := host[:port_from[..port_to]], params := param1=value1[&param2=value2]...[&paramN=valueN]
 *
 * See https://ignite.apache.org/docs/latest/installation/installing-using-docker
 *
 * For simple testing, use `docker run -d -p 10800:10800 apacheignite/ignite` to spin up the DB and then
 * `jdbc:ignite:thin://127.0.0.1` as the JDBC URL, no username or password
 * */
public class IgniteThinDatabaseType extends BaseDatabaseType {
    public String getName() {
        return "Apache Ignite";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:ignite:thin:");
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Apache Ignite");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        return "org.apache.ignite.IgniteJdbcThinDriver";
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new IgniteThinDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new IgniteThinParser(configuration, parsingContext);
    }
}