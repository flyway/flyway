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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.ClassUtils;

import java.sql.Connection;
import java.sql.Types;

public class RedshiftDatabaseType extends DatabaseType {
    private static final String REDSHIFT_JDBC4_DRIVER = "com.amazon.redshift.jdbc4.Driver";
    private static final String REDSHIFT_JDBC41_DRIVER = "com.amazon.redshift.jdbc41.Driver";

    public RedshiftDatabaseType(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getName() {
        return "Redshift";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean supportsReadOnlyTransactions() {
        return true;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:redshift:");
    }

    @Override
    public String getDriverClass(String url) {
        return "com.amazon.redshift.jdbc42.Driver";
    }

    @Override
    public String getBackupDriverClass(String url) {
        if (ClassUtils.isPresent(REDSHIFT_JDBC41_DRIVER, classLoader)) {
            return REDSHIFT_JDBC41_DRIVER;
        }
        return REDSHIFT_JDBC4_DRIVER;
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        if (databaseProductName.startsWith("PostgreSQL")) {
            String selectVersionQueryOutput = getSelectVersionOutput(connection);
            if (databaseProductName.startsWith("PostgreSQL 8") && selectVersionQueryOutput.contains("Redshift")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        return new RedshiftDatabase(configuration, jdbcConnectionFactory



        );
    }

    @Override
    public Parser createParser(Configuration configuration



            , ParsingContext parsingContext) {
        return new RedshiftParser(configuration, parsingContext);
    }
}