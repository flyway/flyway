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
package org.flywaydb.core.internal.database.Cache;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Cache database.
 */
public class CacheDatabase extends Database<CacheConnection> {

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public CacheDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit) {
        super(configuration, connection, originalAutoCommit);
    }

    @Override
    protected CacheConnection getConnection(Connection connection) {
        return new CacheConnection(configuration, this, connection, originalAutoCommit);
    }

    @Override
    public void ensureSupported() {
        String version = majorVersion + "." + minorVersion;
        if (majorVersion < 2015) {
            throw new FlywayDbUpgradeRequiredException("Cache", version, "2015.2");
        }
    }

    @Override
    protected SqlStatementBuilderFactory getSqlStatementBuilderFactory() {
        return CacheSqlStatementBuilderFactory.INSTANCE;
    }

    @Override
    public String getDbName() {
        return "Cache";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT user");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
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
        return cacheQuote(identifier);
    }

    @Override
    public boolean catalogIsSchema() { return false; }

    @Override
    public boolean useSingleConnection() { return false; }

    static String cacheQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    enum CacheSqlStatementBuilderFactory implements SqlStatementBuilderFactory {
        INSTANCE;

        @Override
        public SqlStatementBuilder createSqlStatementBuilder() {
            return new CacheSqlStatementBuilder();
        }
    }
}