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
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class DB2DatabaseType extends DatabaseType {
    public DB2DatabaseType(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getName() {
        return "DB2";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:db2:");
    }

    @Override
    public String getDriverClass(String url) {
        return "com.ibm.db2.jcc.DB2Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("DB2");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        return new DB2Database(configuration, jdbcConnectionFactory



        );
    }

    @Override
    public Parser createParser(Configuration configuration



            , ParsingContext parsingContext) {
        return new DB2Parser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props) {
        props.put("clientProgramName", APPLICATION_NAME);
        props.put("retrieveMessagesFromServerOnGetMessage", "true");
    }
}