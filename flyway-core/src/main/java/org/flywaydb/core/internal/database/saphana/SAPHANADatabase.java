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
package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.Types;

/**
 * SAP HANA database.
 */
public class SAPHANADatabase extends Database<SAPHANAConnection> {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public SAPHANADatabase(FlywayConfiguration configuration, Connection connection



    ) {
        super(configuration, connection, Types.VARCHAR



        );
    }

    @Override
    protected SAPHANAConnection getConnection(Connection connection, int nullType



    ) {
        return new SAPHANAConnection(configuration, this, connection, nullType



        );
    }

    @Override
    protected void ensureSupported() {
        String version = majorVersion + "." + minorVersion;


        if (majorVersion == 1) {
            throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("SAP", "HANA", version);
        }

        if (majorVersion > 2) {
            recommendFlywayUpgrade("SAP HANA", version);
        }

    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SAPHANASqlStatementBuilder(Delimiter.SEMICOLON);
    }

    public String getDbName() {
        return "saphana";
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "1";
    }

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
}