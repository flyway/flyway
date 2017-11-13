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
package org.flywaydb.core.internal.dbsupport.sybasease;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Sybase ASE specific support.
 */
public class SybaseASEDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(SybaseASEDbSupport.class);

    public SybaseASEDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

    @Override
    protected void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 15 || (majorVersion == 15 && minorVersion < 70)) {
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
        return new SybaseASESqlStatementBuilder();
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
        LOG.info("SAP ASE does not support setting the schema for the current session. Default schema NOT changed to " + schema);
    }

    @Override
    public String getCurrentUserFunction() {
        return "user_name()";
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