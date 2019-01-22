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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;

/**
 * Snowflake implementation of a Flyway Database.
 */
public class SnowflakeDatabase extends Database {

    private static final Log LOG = LogFactory.getLog(SnowflakeDatabase.class);

    public SnowflakeDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit) {
        super(configuration, connection, originalAutoCommit);
        LOG.debug("Creating new SnowflakeDatabase");
    }

    //
    // NOTE: the following are overridden in order to provide an implementation
    //

    @Override
    protected org.flywaydb.core.internal.database.base.Connection getConnection(Connection connection) {
        return new SnowflakeConnection(configuration, this, connection, originalAutoCommit);
    }

    @Override
    public void ensureSupported() {
        // using current version (2019-01-22) of 3.10
        ensureDatabaseIsRecentEnough("3.10");
        recommendFlywayUpgradeIfNecessary("3.10");
    }

    @Override
    protected SqlStatementBuilderFactory createSqlStatementBuilderFactory(PlaceholderReplacer placeholderReplacer) {
        return new SnowflakeSqlStatementBuilderFactory(placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "snowflake";
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
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    protected String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    //
    // NOTE: the following are overridden to change the implementation
    //
}
