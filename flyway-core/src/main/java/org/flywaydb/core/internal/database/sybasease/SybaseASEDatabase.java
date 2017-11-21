/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Sybase ASE specific support.
 */
public class SybaseASEDatabase extends Database {
    private static final Log LOG = LogFactory.getLog(SybaseASEDatabase.class);

    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    public SybaseASEDatabase(Connection connection, boolean jconnect) {
        super(new JdbcTemplate(connection, jconnect ? Types.VARCHAR : Types.NULL));
    }

    @Override
    protected void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 15 || (majorVersion == 15 && minorVersion < 7)) {
            throw new FlywayDbUpgradeRequiredException("Sybase ASE", version, "15.7");
        }
        if (majorVersion > 16 || (majorVersion == 16 && minorVersion > 2)) {
            recommendFlywayUpgrade("Sybase ASE", version);
        }
    }

    @Override
    public Schema getSchema(String name) {
        //Sybase does not support schema and changing user on the fly. Always return the same dummy schema.
        return new SybaseASESchema(jdbcTemplate, this, "dbo");
    }

    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SybaseASESqlStatementBuilder(getDefaultDelimiter());
    }

    @Override
    public Delimiter getDefaultDelimiter() {
        return new Delimiter("GO", true);
    }

    @Override
    public String getDbName() {
        return "sybasease";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return "dbo";
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!schemaMessagePrinted) {
            LOG.info("Sybase ASE does not support setting the schema for the current session. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return jdbcTemplate.queryForString("SELECT user_name()");
    }

    @Override
    public boolean supportsDdlTransactions() {
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

    @Override
    protected String doQuote(String identifier) {
        //Sybase doesn't quote identifiers, skip quoting.
        return identifier;
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
