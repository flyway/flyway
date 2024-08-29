/*-
 * ========================LICENSE_START=================================
 * flyway-database-cassandra
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.database.cassandra;

import java.sql.SQLException;
import java.util.Set;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public class CassandraDatabase extends Database<CassandraConnection> {

    public CassandraDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected CassandraConnection doGetConnection(java.sql.Connection connection) {
        return new CassandraConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {

    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    public boolean supportsMultiStatementTransactions() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "true";
    }

    @Override
    public String getBooleanFalse() {
        return "false";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
            "    installed_rank INT,\n" +
            "    version VARCHAR,\n" +
            "    partition VARCHAR,\n" +
            "    description VARCHAR,\n" +
            "    type VARCHAR,\n" +
            "    script VARCHAR,\n" +
            "    checksum INT,\n" +
            "    installed_by VARCHAR,\n" +
            "    installed_on TIMESTAMP,\n" +
            "    execution_time INT,\n" +
            "    success BOOLEAN,\n" +
            "    PRIMARY KEY ((partition), installed_rank));\n" +
            (baseline ? getBaselineStatement(table) + ";\n" : "") +
            "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
    }

    @Override
    public String getSelectStatement(Table table) {
        return "SELECT " + quote("installed_rank")
            + "," + quote("version")
            + "," + quote("description")
            + "," + quote("type")
            + "," + quote("script")
            + "," + quote("checksum")
            + "," + quote("installed_on")
            + "," + quote("installed_by")
            + "," + quote("execution_time")
            + "," + quote("success")
            + " FROM " + table
            + " WHERE " + quote("installed_rank") + " > ?"
            + " ALLOW FILTERING";
    }

    @Override
    public String getInsertStatement(Table table) {
        // Explicitly set installed_on to CURRENT_TIMESTAMP().
        return "INSERT INTO " + table
            + " (" + quote("installed_rank")
            + ", " + quote("version")
            + ", " + quote("partition")
            + ", " + quote("description")
            + ", " + quote("type")
            + ", " + quote("script")
            + ", " + quote("checksum")
            + ", " + quote("installed_by")
            + ", " + quote("installed_on")
            + ", " + quote("execution_time")
            + ", " + quote("success")
            + ")"
            + " VALUES (?, ?, 'flyway', ?, ?, ?, ?, ?, toTimestamp(now()), ?, ?)";
    }

    public String getUpdateStatement(Table table) {
        return super.getUpdateStatement(table) + " and partition='flyway'";
    }

    public Set<String> getSystemSchemas() {
        return Set.of("system", "system_auth", "system_schema", "system_distributed",
            "system_traces", "system_views", "system_virtual_schema");
    }

    @Override
    public Pair<String, Object> getDeleteStatement(Table table, boolean version, String filter) {
        try {
            String selectStatement = "SELECT " + quote("installed_rank")
                + " FROM " + table +
                " WHERE " + quote("success") + " = " + getBooleanFalse() + " AND " +
                (version ?
                    quote("version") + " = ?" :
                    quote("description") + " = ?") + " ALLOW FILTERING";

            String installedRank = jdbcTemplate.queryForString(selectStatement, filter);

            if (!StringUtils.hasText(installedRank)) {
                return null;
            }

            String deleteStatement = "DELETE FROM " + table + " WHERE partition='flyway' AND " + quote("installed_rank") + " = ?";

            return Pair.of(deleteStatement, Integer.valueOf(installedRank));
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair Schema History table. Query statement failed due to " + e.getMessage(), e);
        }
    }
}
