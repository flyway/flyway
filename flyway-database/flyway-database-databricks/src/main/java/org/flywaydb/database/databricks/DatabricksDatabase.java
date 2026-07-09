/*-
 * ========================LICENSE_START=================================
 * flyway-database-databricks
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
package org.flywaydb.database.databricks;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabricksDatabase extends Database<DatabricksConnection> {
    public DatabricksDatabase(final Configuration configuration,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected DatabricksConnection doGetConnection(final Connection connection) {
        return new DatabricksConnection(this, connection);
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT current_user() as user;");
    }

    @Override
    public void ensureSupported(final Configuration configuration) {
        // Always latest Databricks version.
    }

    @Override
    public boolean supportsDdlTransactions() {
        // Databricks is non-transactional
        return false;
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
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean supportsMultiStatementTransactions() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

    @Override
    public String doQuote(final String identifier) {
        return getOpenQuote()
            + StringUtils.replaceAll(identifier, getCloseQuote(), getEscapedQuote())
            + getCloseQuote();
    }

    @Override
    protected String getOpenQuote() {
        return "`";
    }

    @Override
    protected String getCloseQuote() {
        return "`";
    }

    @Override
    public String getEscapedQuote() {
        return "\\`";
    }

    @Override
    public String getRawCreateScript(final Table table, final boolean baseline) {
        final String sql = "CREATE TABLE "
            + table
            + " (\n"
            + "    `installed_rank` INT NOT NULL,\n"
            + "    `version` STRING,\n"
            + "    `description` STRING NOT NULL,\n"
            + "    `type` STRING NOT NULL,\n"
            + "    `script` STRING NOT NULL,\n"
            + "    `checksum` INT,\n"
            + "    `installed_by` STRING NOT NULL,\n"
            + "    `installed_on` TIMESTAMP NOT NULL,\n"
            + "    `execution_time` INT NOT NULL,\n"
            + "    `success` BOOLEAN NOT NULL\n"
            + ");\n"
            + (baseline ? getBaselineStatement(table) + ";\n" : "");
        return sql;
    }

    @Override
    public String getInsertStatement(final Table table) {
        return "INSERT INTO "
            + table
            + " ("
            + quote("installed_rank")
            + ", "
            + quote("version")
            + ", "
            + quote("description")
            + ", "
            + quote("type")
            + ", "
            + quote("script")
            + ", "
            + quote("checksum")
            + ", "
            + quote("installed_by")
            + ", "
            + quote("installed_on")
            + ", "
            + quote("execution_time")
            + ", "
            + quote("success")
            + ")"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(), ?, ?)";
    }
}
