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
package org.flywaydb.database.spanner;

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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.flywaydb.core.internal.util.DataUnits.GIGABYTE;

@CustomLog
public class SpannerDatabase extends Database<SpannerConnection> {
    private static final long ONE_G_FIELD_LIMIT = GIGABYTE.toBytes(1);

    public SpannerDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected SpannerConnection doGetConnection(Connection connection) {
        return new SpannerConnection(this, connection);
    }

    @Override
    public void ensureSupported() {
        recommendFlywayUpgradeIfNecessaryForMajorVersion("1.0");
        if (VersionPrinter.EDITION == Edition.COMMUNITY) {
            long numberOfFields = getNumberOfFields();
            if (numberOfFields > ONE_G_FIELD_LIMIT) {
                throw new FlywayTeamsUpgradeRequiredException("A GCP Spanner database that exceeds the " + ONE_G_FIELD_LIMIT +
                                                                      " field count limit (Calculated field count: " + numberOfFields + ")");
            }

            String usageLimitMessage = "GCP Spanner databases have a " + ONE_G_FIELD_LIMIT + " field count limit in " + Edition.COMMUNITY + ".\n" +
                    "You have used " + numberOfFields + " / " + ONE_G_FIELD_LIMIT + "\n" +
                    "Consider upgrading to " + Edition.ENTERPRISE + " for unlimited usage: " + FlywayDbWebsiteLinks.TEAMS_FEATURES_FOR_CLOUD_SPANNER;

            LOG.info(usageLimitMessage);
        }
    }

    private long getNumberOfFields() {
        long totalNumberOfFields = 0;
        try {
            ResultSet tablesRs = getJdbcMetaData().getTables("", "", null, null);
            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME");
                long rows = jdbcTemplate.queryForLong("SELECT COUNT(*) FROM " + tableName);
                long cols = jdbcTemplate.queryForLong("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                                              "WHERE TABLE_NAME=?", tableName);
                totalNumberOfFields += rows * cols;
            }
        } catch (SQLException ignored) {
        }
        return totalNumberOfFields;
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsMultiStatementTransactions() {
        return false;
    }

    Connection getNewRawConnection() {
        return jdbcConnectionFactory.openConnection();
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
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
    public String getOpenQuote() {
        return "`";
    }

    @Override
    public String getCloseQuote() {
        return "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "" +
                "CREATE TABLE " + table.getName() + " (\n" +
                "    installed_rank INT64 NOT NULL,\n" +
                "    version STRING(50),\n" +
                "    description STRING(200) NOT NULL,\n" +
                "    type STRING(20) NOT NULL,\n" +
                "    script STRING(1000) NOT NULL,\n" +
                "    checksum INT64,\n" +
                "    installed_by STRING(100) NOT NULL,\n" +
                "    installed_on TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp=true),\n" +
                "    execution_time INT64 NOT NULL,\n" +
                "    success BOOL NOT NULL\n" +
                ") PRIMARY KEY (installed_rank DESC);\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "CREATE INDEX " + table.getName() + "_s_idx ON " + table.getName() + " (success);";
    }

    @Override
    public String getInsertStatement(Table table) {
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
                + " VALUES (?, ?, ?, ?, ?, ?, ?, PENDING_COMMIT_TIMESTAMP(), ?, ?)";
    }
}