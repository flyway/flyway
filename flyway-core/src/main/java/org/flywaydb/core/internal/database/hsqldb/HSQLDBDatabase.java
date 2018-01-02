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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.Types;

/**
 * HSQLDB database.
 */
public class HSQLDBDatabase extends Database {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public HSQLDBDatabase(FlywayConfiguration configuration, Connection connection
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
    protected org.flywaydb.core.internal.database.Connection getConnection(Connection connection, int nullType
                                                                           // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                                                                           // [/pro]
    ) {
        return new HSQLDBConnection(configuration, this, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 1 || (majorVersion == 18 && minorVersion < 8)) {
            throw new FlywayDbUpgradeRequiredException("HSQLDB", version, "1.8");
        }
    }

    public String getDbName() {
        return "hsqldb";
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

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new HSQLDBSqlStatementBuilder(getDefaultDelimiter());
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
