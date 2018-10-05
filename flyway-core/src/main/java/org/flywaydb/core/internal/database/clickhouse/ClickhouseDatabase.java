/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.clickhouse;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.hsqldb.HSQLDBConnection;
import org.flywaydb.core.internal.database.hsqldb.HSQLDBSqlStatementBuilder;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.sqlscript.AbstractSqlStatementBuilderFactory;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HSQLDB database.
 */
public class ClickhouseDatabase extends Database<ClickhouseConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public ClickhouseDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected ClickhouseConnection getConnection(Connection connection



    ) {
        return new ClickhouseConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        // ClickHouse doesn't appear to have any concept of users
        return "null";
    }

    @Override
    public final void ensureSupported() {
    }

    @Override
    protected SqlStatementBuilderFactory createSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer



    ) {
        return new ClickhouseSqlStatementBuilderFactory(placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "clickhouse";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

    private static class ClickhouseSqlStatementBuilderFactory extends AbstractSqlStatementBuilderFactory {
        public ClickhouseSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer) {
            super(placeholderReplacer);
        }

        @Override
        public SqlStatementBuilder createSqlStatementBuilder() {
            return new ClickhouseStatementBuilder();
        }
    }
}