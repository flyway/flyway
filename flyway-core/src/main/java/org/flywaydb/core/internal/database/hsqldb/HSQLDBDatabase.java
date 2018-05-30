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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;
import java.util.List;

/**
 * HSQLDB database.
 */
public class HSQLDBDatabase extends Database<HSQLDBConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public HSQLDBDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected HSQLDBConnection getConnection(Connection connection



    ) {
        return new HSQLDBConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 1 || (majorVersion == 1 && minorVersion < 8)) {
            throw new FlywayDbUpgradeRequiredException("HSQLDB", version, "1.8");
        }

        if (majorVersion == 1 || (majorVersion == 2 && minorVersion < 3)) {
        throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("HSQL Development Group", "HSQLDB", version);
        }

        if (majorVersion > 2 || (majorVersion == 2 && minorVersion > 4)) {
            recommendFlywayUpgrade("HSQLDB", version);
        }
    }

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource sqlScriptResource,
                                          PlaceholderReplacer placeholderReplacer, boolean mixed



    ) {
        return new HSQLDBSqlScript(configuration, sqlScriptResource, mixed



                , placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "hsqldb";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    protected boolean supportsChangingCurrentSchema() {
        return true;
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
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}