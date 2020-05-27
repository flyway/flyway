/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL Server database.
 */
public class SQLServerDatabase extends Database<SQLServerConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     */
    public SQLServerDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected SQLServerConnection doGetConnection(Connection connection) {
        return new SQLServerConnection(this, connection);
    }



















    @Override
    public final void ensureSupported() {
        if (isAzure()) {
            ensureDatabaseIsRecentEnough("11.0");

            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("12.0", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

            recommendFlywayUpgradeIfNecessary("12.0");
        } else {
            ensureDatabaseIsRecentEnough("10.0");

            ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("13.0", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

            recommendFlywayUpgradeIfNecessary("15.0");
        }
    }

    @Override
    protected String computeVersionDisplayName(MigrationVersion version) {
        if (isAzure()) {
            return "Azure v" + getVersion().getMajorAsString();
        }

        if (getVersion().isAtLeast("8")) {
            if ("8".equals(getVersion().getMajorAsString())) {
                return "2000";
            }
            if ("9".equals(getVersion().getMajorAsString())) {
                return "2005";
            }
            if ("10".equals(getVersion().getMajorAsString())) {
                if ("0".equals(getVersion().getMinorAsString())) {
                    return "2008";
                }
                return "2008 R2";
            }
            if ("11".equals(getVersion().getMajorAsString())) {
                return "2012";
            }
            if ("12".equals(getVersion().getMajorAsString())) {
                return "2014";
            }
            if ("13".equals(getVersion().getMajorAsString())) {
                return "2016";
            }
            if ("14".equals(getVersion().getMajorAsString())) {
                return "2017";
            }
            if ("15".equals(getVersion().getMajorAsString())) {
                return "2019";
            }
        }
        return super.computeVersionDisplayName(version);
    }

    @Override
    public Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT SUSER_SNAME()");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
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

    /**
     * Escapes this identifier, so it can be safely used in sql queries.
     *
     * @param identifier The identifier to escaped.
     * @return The escaped version.
     */
    private String escapeIdentifier(String identifier) {
        return StringUtils.replaceAll(identifier, "]", "]]");
    }

    @Override
    public String doQuote(String identifier) {
        return "[" + escapeIdentifier(identifier) + "]";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String filegroup = isAzure() || configuration.getTablespace() == null
                ? ""
                : " ON \"" + configuration.getTablespace() + "\"";

        return "CREATE TABLE " + table + " (\n" +
                "    [installed_rank] INT NOT NULL,\n" +
                "    [" + "version] NVARCHAR(50),\n" +
                "    [description] NVARCHAR(200),\n" +
                "    [type] NVARCHAR(20) NOT NULL,\n" +
                "    [script] NVARCHAR(1000) NOT NULL,\n" +
                "    [checksum] INT,\n" +
                "    [installed_by] NVARCHAR(100) NOT NULL,\n" +
                "    [installed_on] DATETIME NOT NULL DEFAULT GETDATE(),\n" +
                "    [execution_time] INT NOT NULL,\n" +
                "    [success] BIT NOT NULL\n" +
                ")" + filegroup + ";\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "ALTER TABLE " + table + " ADD CONSTRAINT [" + table.getName() + "_pk] PRIMARY KEY ([installed_rank]);\n" +
                "CREATE INDEX [" + table.getName() + "_s_idx] ON " + table + " ([success]);\n" +
                "GO\n";
    }

    /**
     * @return Whether this is a SQL Azure database.
     */
    boolean isAzure() {
        return getMainConnection().isAzureConnection();
    }

    /**
     * @return The database engine edition.
     */
    SQLServerEngineEdition getEngineEdition() {
        return getMainConnection().getEngineEdition();
    }

    /**
     * @return Whether this database supports temporal tables
     */
    boolean supportsTemporalTables() {
        // SQL Server 2016+, or Azure  (which has different versioning)
        return isAzure() || getVersion().isAtLeast("13.0");
    }

    /**
     * @return Whether this database supports partitions
     */
    boolean supportsPartitions() {
        return isAzure()
                || SQLServerEngineEdition.ENTERPRISE.equals(getEngineEdition())
                || getVersion().isAtLeast("13");
    }

    /**
     * @return Whether this database supports sequences
     */
    boolean supportsSequences() {
        return getVersion().isAtLeast("11");
    }

    /**
     * Cleans all the objects in this database that need to be done after cleaning schemas.
     *
     * @throws SQLException when the clean failed.
     */
    @Override
    protected void doCleanPostSchemas() throws SQLException {
        if (supportsPartitions()) {
            for (String statement : cleanPartitionSchemes()) {
                jdbcTemplate.execute(statement);
            }

            for (String statement : cleanPartitionFunctions()) {
                jdbcTemplate.execute(statement);
            }
        }
    }

    /**
     * Cleans the Partition Schemes in this database.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanPartitionSchemes() throws SQLException {
        List<String> partitionSchemeNames =
                jdbcTemplate.queryForStringList("SELECT name FROM sys.partition_schemes");
        List<String> statements = new ArrayList<>();
        for (String partitionSchemeName : partitionSchemeNames) {
            statements.add("DROP PARTITION SCHEME " + quote(partitionSchemeName));
        }
        return statements;
    }

    /**
     * Cleans the Partition Functions in this database.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanPartitionFunctions() throws SQLException {
        List<String> partitionFunctionNames =
                jdbcTemplate.queryForStringList("SELECT name FROM sys.partition_functions");
        List<String> statements = new ArrayList<>();
        for (String partitionFunctionName : partitionFunctionNames) {
            statements.add("DROP PARTITION FUNCTION " + quote(partitionFunctionName));
        }
        return statements;
    }
}