/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.singlestore;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;

public class SingleStoreDatabase extends Database<SingleStoreConnection> {

    public SingleStoreDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE " + (getVersion().isAtLeast("7.3") ? "ROWSTORE" : "") +
                " TABLE " + table +
                "(\n" +
                "    `installed_rank` INT NOT NULL,\n" +
                "    `version` VARCHAR(50),\n" +
                "    `description` VARCHAR(200) NOT NULL,\n" +
                "    `type` VARCHAR(20) NOT NULL,\n" +
                "    `script` VARCHAR(1000) NOT NULL,\n" +
                "    `checksum` INT,\n" +
                "    `installed_by` VARCHAR(100) NOT NULL,\n" +
                "    `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    `execution_time` INT NOT NULL,\n" +
                "    `success` BOOL NOT NULL,\n" +
                "    PRIMARY KEY (`installed_rank`),\n" +
                "    INDEX (`success`)\n" +
                ");" + (baseline ? getBaselineStatement(table) : "");
    }

    @Override
    protected SingleStoreConnection doGetConnection(Connection connection) {
        return new SingleStoreConnection(this, connection);
    }

    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("7.1");
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT SUBSTRING_INDEX(USER(),'@',1)");
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
