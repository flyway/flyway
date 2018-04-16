/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Snowflake database.
 */
public class SnowflakeDatabase extends Database {
    private static final Log LOG = LogFactory.getLog(SnowflakeDatabase.class);

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public SnowflakeDatabase(FlywayConfiguration configuration, Connection connection) {
        super(configuration, connection);
    }

    @Override
    protected org.flywaydb.core.internal.database.Connection getConnection(Connection connection) {
        return new SnowflakeConnection(configuration, this, connection);
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if ((majorVersion < 2) || (majorVersion == 2 && minorVersion < 29)) {
            throw new FlywayDbUpgradeRequiredException("Snowflake", version, "2.29");
        }
    }

    @Override
    protected SqlScript doCreateSqlScript(Resource sqlScriptResource, String sqlScriptSource, boolean mixed) {
        return new SnowflakeSqlScript(sqlScriptResource, sqlScriptSource, mixed);
    }

    public String getDbName() {
        return "snowflake";
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

}
