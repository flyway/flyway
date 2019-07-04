/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import java.sql.Connection;

/**
 * Google Cloud Spanner database.
 */
public class CloudSpannerDatabase extends Database<CloudSpannerConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration         The Flyway configuration.
     * @param jdbcConnectionFactory The connection factory.
     */
    public CloudSpannerDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory) {
        super(configuration, jdbcConnectionFactory);
    }

    @Override
    protected CloudSpannerConnection doGetConnection(Connection connection) {
        return new CloudSpannerConnection(this, connection);
    }

    @Override
    public final void ensureSupported() {
        final MigrationVersion version = getVersion();
        if (version.isMajorNewerThan("1")) {
            throw new FlywayDbUpgradeRequiredException(DatabaseType.CLOUDSPANNER, version.toString(), "1.0");
        }
        recommendFlywayUpgradeIfNecessary("1.0");
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "true";
    }

    public String getBooleanFalse() {
        return "false";
    }

    @Override
    public String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    public boolean useSingleConnection() {
        return true;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return false;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
                "    `installed_rank` INT64 NOT NULL,\n" +
                "    `version` STRING(50),\n" +
                "    `description` STRING(200) NOT NULL,\n" +
                "    `type` STRING(20) NOT NULL,\n" +
                "    `script` STRING(1000) NOT NULL,\n" +
                "    `checksum` INT64,\n" +
                "    `installed_by` STRING(100) NOT NULL,\n" +
                "    `installed_on` TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp=true),\n" +
                "    `execution_time` INT64 NOT NULL,\n" +
                "    `success` BOOL NOT NULL\n" +
                ") PRIMARY KEY (`installed_rank`);\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
               // "SET_CONNECTION_PROPERTY AsyncDdlOperations=true;\n"+
                "CREATE INDEX " + doQuote(table.getName() + "_s_idx") +" ON " + table + " (`success`)";
              //+  "RESET_CONNECTION_PROPERTY AsyncDdlOperations";
    }

    public String getInsertStatement(Table table) {
        return "INSERT INTO " + table
                + "\n (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("installed_on")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")\n"
             // temporary commented, the JDBC driver does not support this function invocation
             //   + " VALUES (?, ?, ?, ?, ?, ?, ?, PENDING_COMMIT_TIMESTAMP(), ?, ?)"
             // Function calls such as for example GET_TIMESTAMP() are not allowed in client side insert/update statements
                + " SELECT ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(), ?, ?";
    }

}