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
import java.sql.SQLException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Note: The necessary driver is not available via Maven. See flywaydb.org documentation for where to get it from.
 */
@CustomLog
public class BigQueryDatabase extends Database<BigQueryConnection> {
    private static final long TEN_GB_DATASET_SIZE_LIMIT = 10L * 1024 * 1024 * 1024;
    private static final long NINE_GB_DATASET_SIZE = 9L * 1024 * 1024 * 1024;

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
            long totalDatasetSize = 0;
            for (String dataset : configuration.getSchemas()) {
                totalDatasetSize += getDatasetSize(dataset);
            }

            String byteCount = humanReadableByteCountSI(totalDatasetSize);

            if (totalDatasetSize > TEN_GB_DATASET_SIZE_LIMIT) {
                throw new FlywayTeamsUpgradeRequiredException("Google BigQuery databases that exceed the 10 GB dataset size limit (Calculated size: " + byteCount + ")");
            }

            String usageLimitMessage =
                    "Google BigQuery databases have a 10 GB dataset size limit in Flyway " +
                            Edition.COMMUNITY + ".\n" +
                            "You have used " + byteCount + " / 10 GB\n" +
                            "Consider upgrading to Flyway " + Edition.ENTERPRISE + " for unlimited usage: " +
                            FlywayDbWebsiteLinks.TEAMS_FEATURES_FOR_BIG_QUERY;

            if (totalDatasetSize >= NINE_GB_DATASET_SIZE) {
                LOG.warn(usageLimitMessage);
            } else {
                LOG.info(usageLimitMessage);
            }
        }
    }

    // From https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
    // The most copied StackOverflow answer of all time!
    private String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    private long getDatasetSize(String dataset) {
        try {
            return jdbcTemplate.queryForLong("select sum(size_bytes) from " + dataset + ".__TABLES__");
        } catch (SQLException e) {
            // Ignore the case when the query fails to execute
            return 0;
        }
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
        return bigQueryQuote(identifier);
    }

    static String bigQueryQuote(String identifier) {
        return "`" + StringUtils.replaceAll(identifier, "`", "\\`") + "`";
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