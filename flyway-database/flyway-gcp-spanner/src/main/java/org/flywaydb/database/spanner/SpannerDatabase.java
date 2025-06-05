/*-
 * ========================LICENSE_START=================================
 * flyway-gcp-spanner
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.database.spanner;

import lombok.CustomLog;
import lombok.Setter;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_GOOGLE_SPANNER;
import static org.flywaydb.core.internal.util.DataUnits.GIGABYTE;

@CustomLog
public class SpannerDatabase extends Database<SpannerConnection> {
    private static final long TEN_GB_DATABASE_SIZE_LIMIT = GIGABYTE.toBytes(10);
    private static final long NINE_GB_DATABASE_SIZE = GIGABYTE.toBytes(9);
    @Setter
    String statsTableName = "spanner_sys.table_sizes_stats_1hour";

    public SpannerDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected SpannerConnection doGetConnection(Connection connection) {
        return new SpannerConnection(this, connection);
    }

    @Override
    public void ensureSupported(Configuration configuration) {
        if (!LicenseGuard.isLicensed(configuration, Tier.PREMIUM)) {
            long databaseSize = getDatabaseSize();
            if (databaseSize > TEN_GB_DATABASE_SIZE_LIMIT) {
                throw new FlywayEditionUpgradeRequiredException(LicenseGuard.getTier(configuration),
                    "A GCP Spanner database that exceeds the 10 GB database size limit " +
                        "(Calculated size: " + GIGABYTE.toHumanReadableString(databaseSize) + ")");
            }

            String usageLimitMessage = "GCP Spanner databases have a 10 GB database size limit in " + Tier.COMMUNITY.getDisplayName() + ".\n" +
                    "You have used " + GIGABYTE.toHumanReadableString(databaseSize) + " / 10 GB\n" +
                    "Consider upgrading to " + Tier.ENTERPRISE.getDisplayName() + " for unlimited usage: " + FlywayDbWebsiteLinks.TEAMS_FEATURES_FOR_CLOUD_SPANNER;

            if (databaseSize >= NINE_GB_DATABASE_SIZE) {
                LOG.warn(usageLimitMessage);
            } else {
                LOG.info(usageLimitMessage);
            }
        }
    }

    long getDatabaseSize() {
        long totalDatabaseSize = 0;
        try {
            totalDatabaseSize += jdbcTemplate.queryForLong("SELECT SUM(USED_BYTES) FROM " + statsTableName + " WHERE INTERVAL_END = (SELECT MAX(INTERVAL_END) FROM " + statsTableName + ")");
        } catch (SQLException ignored) {
        }
        return totalDatabaseSize;
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

    @Override
    public String getDatabaseHosting() {
        return DATABASE_HOSTING_GOOGLE_SPANNER;
    }
}
