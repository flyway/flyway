/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
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
package org.flywaydb.database.sqlserver.fabricDataWarehouse;

import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_FABRIC_DATA_WAREHOUSE;

import java.sql.Connection;
import java.util.Date;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.database.sqlserver.SQLServerConnection;
import org.flywaydb.database.sqlserver.SQLServerDatabase;

public class FabricDataWarehouseDatabase extends SQLServerDatabase {

    public FabricDataWarehouseDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected SQLServerConnection doGetConnection(Connection connection) {
        return new FabricDataWarehouseConnection(this, connection);
    }

    @Override
    protected String computeVersionDisplayName(MigrationVersion version) {
        return getVersion().getMajorAsString();
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsMultiStatementTransactions() {
        return false;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {

        return "CREATE TABLE " + table + " (\n" +
                "    [installed_rank] INT NOT NULL,\n" +
                "    [" + "version] VARCHAR(50),\n" +
                "    [description] VARCHAR(200),\n" +
                "    [type] VARCHAR(20) NOT NULL,\n" +
                "    [script] VARCHAR(1000) NOT NULL,\n" +
                "    [checksum] INT,\n" +
                "    [installed_by] VARCHAR(100) NOT NULL,\n" +
                "    [installed_on] DATETIME2(6) NOT NULL,\n" +
                "    [execution_time] INT NOT NULL,\n" +
                "    [success] BIT NOT NULL\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "ALTER TABLE " + table + " ADD CONSTRAINT [" + table.getName() + "_pk] PRIMARY KEY NONCLUSTERED (installed_rank) NOT ENFORCED;\n" +
                "GO\n";
    }

    @Override
    public String getInsertStatement(Table table) {
        String currentDateTime = new java.sql.Timestamp(new Date().getTime()).toString();
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("installed_on")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, '" + currentDateTime + "', ?, ?)";
    }

    @Override
    public String getDatabaseHosting() {
        return DATABASE_HOSTING_FABRIC_DATA_WAREHOUSE;
    }
}
