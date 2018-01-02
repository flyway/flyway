/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
                           // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                           // [/pro]
    ) {
        super(configuration, connection, Types.VARCHAR
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }

    @Override
    protected SAPHANAConnection getConnection(Connection connection, int nullType
                                              // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                                              // [/pro]
    ) {
        return new SAPHANAConnection(configuration, this, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }

    @Override
    protected void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        // [enterprise-not]
        //if (majorVersion == 1) {
        //    throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("SAP", "HANA", version);
        //}
        // [/enterprise-not]
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
