/*
 * Copyright 2010-2020 Boxfuse GmbH
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
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ClickHouse database.
 */
public class ClickHouseDatabase extends Database<ClickHouseConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration         The Flyway configuration.
     * @param jdbcConnectionFactory The connection factory.
     */
    public ClickHouseDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory) {
        super(configuration, jdbcConnectionFactory);
    }

    @Override
    protected ClickHouseConnection doGetConnection(Connection connection) {
        return new ClickHouseConnection(this, connection);
    }

    @Override
    public final void ensureSupported() {
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + "(" +
                "    installed_rank Int32," +
                "    version Nullable(String)," +
                "    description String," +
                "    type String," +
                "    script String," +
                "    checksum Nullable(Int32)," +
                "    installed_by String," +
                "    installed_on DateTime DEFAULT now()," +
                "    execution_time Int32," +
                "    success UInt8," +
                "    CONSTRAINT success CHECK success in (0,1)" +
                ") ENGINE = TinyLog;" +
                (baseline ? getBaselineStatement(table) + ";" : "");
    }

    /**
     * ClickHouse does not support deleting and updating,
     * so, we perform the deletion by copying only those rows that satisfy the condition into the newly created table
     */
    @Override
    protected String getRawDeleteScript(Table table) {
        String backupTableName = quote(table.getSchema().getName(), table.getName() + "_backup");

        return "DROP TABLE IF EXISTS " + backupTableName + ";" +
                "RENAME TABLE " + table + " TO " + backupTableName + ";" +
                getRawCreateScript(table, false) + ";" +
                "INSERT INTO " + table + " SELECT * FROM " + backupTableName + " WHERE success > 0 ORDER BY installed_rank;" +
                "DROP TABLE " + backupTableName + ";";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT currentUser()");
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
        return identifier;
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}
