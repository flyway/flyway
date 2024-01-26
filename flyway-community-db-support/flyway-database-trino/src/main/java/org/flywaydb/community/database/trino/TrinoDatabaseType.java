/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.community.database.trino;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

public class TrinoDatabaseType
        extends BaseDatabaseType
{
    @Override
    public String getName()
    {
        return "Trino";
    }

    @Override
    public int getNullType()
    {
        return Types.NULL;
    }

    @Override
    public boolean handlesJDBCUrl(String url)
    {
        return url.startsWith("jdbc:trino:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader)
    {
        return "io.trino.jdbc.TrinoDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(
            String databaseProductName,
            String databaseProductVersion,
            Connection connection)
    {
        return databaseProductName.startsWith("Trino");
    }

    @Override
    public Database createDatabase(
            Configuration configuration,
            JdbcConnectionFactory jdbcConnectionFactory,
            StatementInterceptor statementInterceptor)
    {
        return new TrinoDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(
            Configuration configuration,
            ResourceProvider resourceProvider,
            ParsingContext parsingContext)
    {
        return new TrinoParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader)
    {
        props.put("source", APPLICATION_NAME);
    }

    @Override
    public boolean detectUserRequiredByUrl(String url)
    {
        return !url.contains("user=");
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url)
    {
        return !url.contains("password=");
    }
}
