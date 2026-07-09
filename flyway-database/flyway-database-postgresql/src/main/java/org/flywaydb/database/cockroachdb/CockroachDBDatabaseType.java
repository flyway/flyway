/*-
 * ========================LICENSE_START=================================
 * flyway-database-postgresql
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
package org.flywaydb.database.cockroachdb;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.database.postgresql.authentication.PgpassFileReader;

import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DefaultExecutionStrategy;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StringUtils;
import lombok.CustomLog;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

@CustomLog
public class CockroachDBDatabaseType extends BaseDatabaseType {




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
    public boolean handlesJDBCUrl(final String url) {
        return url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:p6spy:postgresql:");
    }

    @Override
    public int getPriority() {
        // Must be checked ahead of the vanilla PostgreSQLDatabaseType
        return 1;
    }

    @Override
    public String getDriverClass(final String url, final ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:postgresql:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "org.postgresql.Driver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(final String databaseProductName,
        final String databaseProductVersion,
        final Connection connection) {
        if (databaseProductName.startsWith("PostgreSQL")) {
            final String selectVersionQueryOutput = BaseDatabaseType.getSelectVersionOutput(connection);
            return selectVersionQueryOutput.contains("CockroachDB");
        }

        return false;
    }

    @Override
    public Database createDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        return new CockroachDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(final Configuration configuration,
        final ResourceProvider resourceProvider,
        final ParsingContext parsingContext) {
        return new CockroachDBParser(configuration, parsingContext);
    }

    @Override
    public DatabaseExecutionStrategy createExecutionStrategy(final Connection connection) {
        if (connection == null) {
            return new DefaultExecutionStrategy();
        }

        return new CockroachDBRetryingStrategy();
    }

    @Override
    public ExecutionTemplate createTransactionalExecutionTemplate(final Connection connection,
        final boolean rollbackOnException) {
        return new CockroachRetryingTransactionalExecutionTemplate(connection, rollbackOnException);
    }

    @Override
    public void setDefaultConnectionProps(final String url, final Properties props, final ClassLoader classLoader) {
        props.put("applicationName", BaseDatabaseType.APPLICATION_NAME);
    }



















    @Override
    public Properties getExternalAuthProperties(final String url, final String username) {
        final PgpassFileReader pgpassFileReader = new PgpassFileReader();

                if (pgpassFileReader.getPgpassFilePath() != null) {
                    LOG.info(org.flywaydb.core.internal.license.FlywayUpgradeMessage.generate(
                            "pgpass file '" + pgpassFileReader.getPgpassFilePath() + "'",
                            "use this for database authentication"));
                }
                return super.getExternalAuthProperties(url, username);







    }
}
