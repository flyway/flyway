/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.snowflake;

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

public class SnowflakeDatabaseType extends BaseDatabaseType {
    @Override
    public String getName() {
        return "Snowflake";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }








    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:snowflake:") || url.startsWith("jdbc:p6spy:snowflake:")
                || url.startsWith("jdbc:otel:snowflake:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:snowflake:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        if (url.startsWith("jdbc:otel:snowflake:")) {
            return "io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver";
        }
        return "net.snowflake.client.jdbc.SnowflakeDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("Snowflake");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new SnowflakeDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new SnowflakeParser(configuration, parsingContext);
    }

    @Override
    public boolean detectUserRequiredByUrl(String url) {
        // Using Snowflake private-key auth instead of password allows user to be passed on URL
        return !url.contains("user=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {
        // Using Snowflake private-key auth instead of password
        return !url.contains("private_key_file=");
    }
}
