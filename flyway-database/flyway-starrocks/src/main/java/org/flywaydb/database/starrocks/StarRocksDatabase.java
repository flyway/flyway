/*-
 * ========================LICENSE_START=================================
 * flyway-starrocks
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
package org.flywaydb.database.starrocks;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * StarRocks-specific database.
 * <p>
 * Key differences from MySQLDatabase:
 * <ul>
 *   <li>Schema history table uses ENGINE=OLAP with DUPLICATE KEY</li>
 *   <li>StarRocks does not support DDL transactions</li>
 *   <li>No Percona XtraDB / GTID / wsrep checks</li>
 * </ul>
 */
public class StarRocksDatabase extends Database<StarRocksConnection> {

    public StarRocksDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public String getRawCreateScript(final Table table, final boolean baseline) {
        return "CREATE TABLE "
            + table
            + "(\n"
            + "    `installed_rank` INT NOT NULL,\n"
            + "    `version` VARCHAR(50),\n"
            + "    `description` VARCHAR(200) NOT NULL,\n"
            + "    `type` VARCHAR(20) NOT NULL,\n"
            + "    `script` VARCHAR(1000) NOT NULL,\n"
            + "    `checksum` INT,\n"
            + "    `installed_by` VARCHAR(100) NOT NULL,\n"
            + "    `installed_on` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
            + "    `execution_time` INT NOT NULL,\n"
            + "    `success` BOOLEAN NOT NULL\n"
            + ")\n"
            + "ENGINE=OLAP\n"
            + "DUPLICATE KEY(`installed_rank`)\n"
            + "DISTRIBUTED BY HASH(`installed_rank`) BUCKETS 1\n"
            + "PROPERTIES (\n"
            + "    \"replication_num\" = \"1\"\n"
            + ");\n"
            + (baseline ? getBaselineStatement(table) : "");
    }

    @Override
    protected StarRocksConnection doGetConnection(final Connection connection) {
        return new StarRocksConnection(this, connection);
    }

    @Override
    public void ensureSupported(final Configuration configuration) {
        ensureDatabaseIsRecentEnough("3.0");
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString(
            "SELECT SUBSTRING_INDEX(USER(),'@',1)");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
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
    public String getOpenQuote() {
        return "`";
    }

    @Override
    public String getCloseQuote() {
        return "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }
}
