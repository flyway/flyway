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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DefaultExecutionStrategy;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.DatabaseType;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class CockroachDBDatabaseType extends DatabaseType {
    public CockroachDBDatabaseType(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getName() {
        return "CockroachDB";
    }

    @Override
    public int getNullType() {
        return Types.NULL;
    }

    @Override
    public boolean supportsReadOnlyTransactions() {
        return false;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:postgresql:");
    }

    @Override
    public String getDriverClass(String url) {
        return "org.postgresql.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        if (databaseProductName.startsWith("PostgreSQL")) {
            String selectVersionQueryOutput = getSelectVersionOutput(connection);
            return selectVersionQueryOutput.contains("CockroachDB");
        }

        return false;
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        return new CockroachDBDatabase(configuration, jdbcConnectionFactory



        );
    }

    @Override
    public Parser createParser(Configuration configuration



            , ParsingContext parsingContext) {
        return new CockroachDBParser(configuration, parsingContext);
    }

    @Override
    public DatabaseExecutionStrategy createExecutionStrategy(Connection connection) {
        if (connection == null) {
            return new DefaultExecutionStrategy();
        }

        return new CockroachDBRetryingStrategy();
    }

    @Override
    public ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        return new CockroachRetryingTransactionalExecutionTemplate(connection, rollbackOnException);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props) {
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
}