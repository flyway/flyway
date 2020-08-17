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
package org.flywaydb.core.internal.database.firebird;

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

public class FirebirdDatabaseType extends DatabaseType {
    public FirebirdDatabaseType(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getName() {
        return "Firebird";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:firebird:") || url.startsWith("jdbc:firebirdsql:");
    }

    @Override
    public String getDriverClass(String url) {
        if (url.startsWith("jdbc:firebird:")) {
            return "org.firebirdsql.jdbc.FBDriver";
        } else {
            return "org.firebirdsql.jdbc.FBDriver";
        }
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Firebird");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        return new FirebirdDatabase(configuration, jdbcConnectionFactory



        );
    }

    @Override
    public Parser createParser(Configuration configuration



            , ParsingContext parsingContext) {
        return new FirebirdParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props) {
        props.put("processName", APPLICATION_NAME);
    }
}