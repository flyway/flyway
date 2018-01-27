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
package org.flywaydb.core.internal.database.nuodb;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;

import java.sql.Connection;
import java.sql.Types;

/**
 * NuoDB database.
 */
public class NuoDBDatabase extends Database<NuoDBConnection> {

    public NuoDBDatabase(FlywayConfiguration configuration, Connection connection) {
        super(configuration, connection, Types.NULL);
    }

    @Override
    protected NuoDBConnection getConnection(Connection connection, int nullType) {
        return new NuoDBConnection(configuration, this, connection, nullType);
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 3) {
            throw new FlywayDbUpgradeRequiredException("NuoDB", version, "3.0");
        }

        if (majorVersion > 3) {
            recommendFlywayUpgrade("NuoDB", version);
        }
    }

    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new NuoDBSqlStatementBuilder(getDefaultDelimiter());
    }

    @Override
    public String getDbName() {
        return "nuodb";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
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
    protected String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
