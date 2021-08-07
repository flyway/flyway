/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.sqlserver.synapse;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.sqlserver.SQLServerConnection;
import org.flywaydb.core.internal.database.sqlserver.SQLServerDatabase;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.util.Date;

/**
 * Azure Synapse database.
 */
public class SynapseDatabase extends SQLServerDatabase {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     */
    public SynapseDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected SQLServerConnection doGetConnection(Connection connection) {
        return new SynapseConnection(this, connection);
    }

    @Override
    protected String computeVersionDisplayName(MigrationVersion version) {
        return "Azure Synapse v" + getVersion().getMajorAsString();
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
    protected boolean supportsPartitions() {
        return false;
    }

    @Override
    protected boolean supportsSynonyms() {
        return false;
    }

    @Override
    protected boolean supportsRules() {
        return false;
    }

    @Override
    protected boolean supportsTypes() {
        return false;
    }

    @Override
    protected boolean supportsSequences() {
        return false;
    }

    @Override
    protected boolean supportsTriggers() {
        return false;
    }

    @Override
    protected boolean supportsAssemblies() {
        return false;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {

        return "CREATE TABLE " + table + " (\n" +
                "    [installed_rank] INT NOT NULL,\n" +
                "    [" + "version] NVARCHAR(50),\n" +
                "    [description] NVARCHAR(200),\n" +
                "    [type] NVARCHAR(20) NOT NULL,\n" +
                "    [script] NVARCHAR(1000) NOT NULL,\n" +
                "    [checksum] INT,\n" +
                "    [installed_by] NVARCHAR(100) NOT NULL,\n" +
                "    [installed_on] DATETIME NOT NULL,\n" +
                "    [execution_time] INT NOT NULL,\n" +
                "    [success] BIT NOT NULL\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "ALTER TABLE " + table + " ADD CONSTRAINT [" + table.getName() + "_pk] PRIMARY KEY NONCLUSTERED (installed_rank) NOT ENFORCED;\n" +
                "CREATE INDEX [" + table.getName() + "_s_idx] ON " + table + " ([success]);\n" +
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

}