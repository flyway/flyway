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
package org.flywaydb.core.internal.database.phoenix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;

public class PhoenixDatabase extends Database<PhoenixConnection> {
    /**
     * Creates a new Database instance with this JdbcTemplate.
     *
     * @param configuration      The Flyway configuration.
     * @param connection         The main connection to use.
     * @param originalAutoCommit The original auto-commit state for connections to this database.
     */
    public PhoenixDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit) {
        super(configuration, connection, originalAutoCommit);
    }

    @Override
    protected PhoenixConnection getConnection(Connection connection) {
        return new PhoenixConnection(configuration, this, connection, originalAutoCommit);
    }

    @Override
    public void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 4 || (majorVersion == 4 && minorVersion < 8)) {
            throw new FlywayDbUpgradeRequiredException("Phoenix", version, "4.8");
        }
        if (majorVersion > 4 || (majorVersion == 4 && minorVersion > 14)) {
            recommendFlywayUpgrade("Phoenix", version);
        }
    }

    @Override
    protected SqlStatementBuilderFactory getSqlStatementBuilderFactory() {
        return PhoenixSqlStatementBuilderFactory.INSTANCE;
    }

    @Override
    public String getInsertStatement(Table table) {
        return "UPSERT INTO " + table
                + " (" + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + "," + quote("installed_on") // Phoenix supports constant defaults only
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    public String getUpdateStatement(Table table) {
        return "UPSERT INTO " + table
                + " (description, type, checksum)"
                + " VALUES (?, ?, ?)"
                + " WHERE " + quote("version") + "=?";
    }

    @Override
    public String getDbName() {
        return "phoenix";
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

    private enum PhoenixSqlStatementBuilderFactory implements SqlStatementBuilderFactory {
        INSTANCE;

        @Override
        public SqlStatementBuilder createSqlStatementBuilder() {
            return new PhoenixSqlStatementBuilder();
        }
    }
}
