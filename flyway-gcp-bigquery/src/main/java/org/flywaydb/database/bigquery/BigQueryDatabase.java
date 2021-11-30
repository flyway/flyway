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
package org.flywaydb.database.bigquery;

import lombok.CustomLog;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.flywaydb.core.internal.util.DataUnits.GIGABYTE;

/**
 * Note: The necessary driver is not available via Maven. See flywaydb.org documentation for where to get it from.
 */
@CustomLog
public class BigQueryDatabase extends Database<BigQueryConnection> {
    private static final long TEN_GB_DATABASE_SIZE_LIMIT = GIGABYTE.toBytes(10);
    private static final long NINE_GB_DATABASE_SIZE = GIGABYTE.toBytes(9);

    public BigQueryDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected BigQueryConnection doGetConnection(Connection connection) {
        return new BigQueryConnection(this, connection);
    }

    @Override
    public final void ensureSupported() {
        if (VersionPrinter.EDITION == Edition.COMMUNITY) {
            long databaseSize = getDatabaseSize();
            if (databaseSize > TEN_GB_DATABASE_SIZE_LIMIT) {
                throw new FlywayTeamsUpgradeRequiredException("A Google BigQuery database that exceeds the 10 GB database size limit " +
                                                                      "(Calculated size: " + GIGABYTE.toHumanReadableString(databaseSize) + ")");
            }

            String usageLimitMessage = "Google BigQuery databases have a 10 GB database size limit in " + Edition.COMMUNITY + ".\n" +
                    "You have used " + GIGABYTE.toHumanReadableString(databaseSize) + " / 10 GB\n" +
                    "Consider upgrading to " + Edition.ENTERPRISE + " for unlimited usage: " + FlywayDbWebsiteLinks.TEAMS_FEATURES_FOR_BIG_QUERY;

            if (databaseSize >= NINE_GB_DATABASE_SIZE) {
                LOG.warn(usageLimitMessage);
            } else {
                LOG.info(usageLimitMessage);
            }
        }
    }

    private long getDatabaseSize() {
        long totalDatabaseSize = 0;
        try {
            ResultSet schemaRs = getJdbcMetaData().getSchemas();
            while (schemaRs.next()) {
                totalDatabaseSize += jdbcTemplate.queryForLong("select sum(size_bytes) from " + schemaRs.getString("TABLE_SCHEM") + ".__TABLES__");
            }
        } catch (SQLException ignored) {
        }
        return totalDatabaseSize;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
                "    `installed_rank` INT64 NOT NULL,\n" +
                "    `version` STRING,\n" +
                "    `description` STRING NOT NULL,\n" +
                "    `type` STRING NOT NULL,\n" +
                "    `script` STRING NOT NULL,\n" +
                "    `checksum` INT64,\n" +
                "    `installed_by` STRING NOT NULL,\n" +
                "    `installed_on` TIMESTAMP,\n" + // BigQuery does not support default value
                "    `execution_time` INT64 NOT NULL,\n" +
                "    `success` BOOL NOT NULL\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "");
    }

    @Override
    public String getInsertStatement(Table table) {
        // Explicitly set installed_on to CURRENT_TIMESTAMP().
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
                + " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(), ?, ?)";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT SESSION_USER() as user;");
    }

    @Override
    public boolean supportsDdlTransactions() {
        // BigQuery is non-transactional
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        // BigQuery has no concept of a current schema
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
    public String doQuote(String identifier) {
        return getOpenQuote() + StringUtils.replaceAll(identifier, getCloseQuote(), getEscapedQuote()) + getCloseQuote();
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
    public String getEscapedQuote() {
        return "\\`";
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
}