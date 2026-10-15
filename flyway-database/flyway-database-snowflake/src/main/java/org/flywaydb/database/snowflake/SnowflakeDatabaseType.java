/*-
 * ========================LICENSE_START=================================
 * flyway-database-snowflake
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
package org.flywaydb.database.snowflake;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.PlainExecutionTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

public class SnowflakeDatabaseType extends BaseDatabaseType {
    private static final String RESULT_FORMAT_PROPERTY = "JDBC_QUERY_RESULT_FORMAT";
    private static final String APPLICATION_PROPERTY = "application";

    @Override
    public String getName() {
        return "Snowflake";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean supportsReadOnlyTransactions() {
        return false;
    }

    @Override
    public boolean handlesJDBCUrl(final String url) {
        return url.startsWith("jdbc:snowflake:") || url.startsWith("jdbc:p6spy:snowflake:");
    }

    @Override
    public String getDriverClass(final String url, final ClassLoader classLoader) {
        if (url.startsWith("jdbc:p6spy:snowflake:")) {
            return "com.p6spy.engine.spy.P6SpyDriver";
        }
        return "net.snowflake.client.api.driver.SnowflakeDriver";
    }

    @Override
    public String getBackupDriverClass(final String url, final ClassLoader classLoader) {
        return "net.snowflake.client.jdbc.SnowflakeDriver";
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(final String databaseProductName,
        final String databaseProductVersion,
        final Connection connection) {
        return databaseProductName.startsWith("Snowflake");
    }

    @Override
    public Database createDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        return new SnowflakeDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(final Configuration configuration,
        final ResourceProvider resourceProvider,
        final ParsingContext parsingContext) {
        return new SnowflakeParser(configuration, parsingContext);
    }

    @Override
    public ExecutionTemplate createTransactionalExecutionTemplate(final Connection connection,
        final boolean rollbackOnException) {
        return new PlainExecutionTemplate();
    }

    @Override
    public void setOverridingConnectionProps(final String url, final Map<String, String> props) {
        // The Snowflake driver ignores properties inherited from defaults and lets passed properties override URL
        // parameters, so these must be set here rather than in setDefaultConnectionProps.
        useJsonResultFormat(props);
        identifyFlywayUnlessApplicationSet(url, props);
    }

    private static void useJsonResultFormat(final Map<String, String> props) {
        props.keySet().removeIf(RESULT_FORMAT_PROPERTY::equalsIgnoreCase);
        props.put(RESULT_FORMAT_PROPERTY, "JSON");
    }

    private static void identifyFlywayUnlessApplicationSet(final String url, final Map<String, String> props) {
        if (!isSetInUrl(url, APPLICATION_PROPERTY) && !isSetInProps(props, APPLICATION_PROPERTY)) {
            props.put(APPLICATION_PROPERTY, APPLICATION_NAME);
        }
    }

    private static boolean isSetInProps(final Map<String, String> props, final String property) {
        return props.keySet().stream().anyMatch(property::equalsIgnoreCase);
    }

    private static boolean isSetInUrl(final String url, final String property) {
        final int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return false;
        }
        return Arrays.stream(url.substring(queryStart + 1).split("&"))
            .map(parameter -> parameter.split("=", 2)[0])
            .anyMatch(property::equalsIgnoreCase);
    }
}
