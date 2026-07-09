/*-
 * ========================LICENSE_START=================================
 * flyway-database-cassandra
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.cassandra;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;

import static org.flywaydb.core.internal.util.DeprecationUtils.DeprecatedFeatures.CASSANDRA_JDBC;
import static org.flywaydb.core.internal.util.DeprecationUtils.printDeprecationNotice;

import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;

public class CassandraDatabaseType extends BaseDatabaseType {
    private static boolean deprecationLogged;

    @Override
    public String getName() {
        return "Cassandra";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    public boolean handlesJDBCUrl(final String url) {
        return url.startsWith("jdbc:cassandra:") || url.startsWith("jdbc:p6spy:cassandra");
    }

    @Override
    public String getDriverClass(final String url, final ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:cassandra")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }

        return "com.ing.data.cassandra.jdbc.CassandraDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(final String databaseProductName,
        final String databaseProductVersion,
        final Connection connection) {
        if (databaseProductName.startsWith("Cassandra")) {
            if (!deprecationLogged) {
                printDeprecationNotice(CASSANDRA_JDBC);
                deprecationLogged = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public Database createDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        return new CassandraDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(final Configuration configuration,
        final ResourceProvider resourceProvider,
        final ParsingContext parsingContext) {
        return new CassandraParser(configuration, parsingContext);
    }
}
