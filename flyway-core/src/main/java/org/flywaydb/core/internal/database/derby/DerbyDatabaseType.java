/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

public class DerbyDatabaseType extends DatabaseType {
    public DerbyDatabaseType(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getName() {
        return "Derby";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:derby:");
    }

    @Override
    public String getDriverClass(String url) {
        if (url.startsWith("jdbc:derby://")) {
            return "org.apache.derby.jdbc.ClientDriver";
        }

        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Apache Derby");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        return new DerbyDatabase(configuration, jdbcConnectionFactory



        );
    }

    @Override
    public Parser createParser(Configuration configuration



            , ParsingContext parsingContext) {
        return new DerbyParser(configuration, parsingContext);
    }

    @Override
    public void shutdownDatabase(String url, Driver driver) {

        // only do this on the embedded version
        if (!url.startsWith("jdbc:derby://")) {
            try {
                int i = url.indexOf(";");
                String shutdownUrl = (i < 0 ? url : url.substring(0, i)) + ";shutdown=true";

                driver.connect(shutdownUrl, new Properties());
            } catch (SQLException e) {
                LOG.debug("Unexpected error on Derby Embedded Database shutdown: " + e.getMessage());
            }
        }
    }
}