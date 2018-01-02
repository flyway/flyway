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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL database.
 */
public class PostgreSQLDatabase extends Database {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public PostgreSQLDatabase(FlywayConfiguration configuration, Connection connection
                              // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                              // [/pro]
    ) {
        super(configuration, connection, Types.NULL
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
        return new PostgreSQLConnection(configuration, this, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 9) {
            throw new FlywayDbUpgradeRequiredException("PostgreSQL", version, "9.0");
        }
        // [enterprise-not]
        //if (majorVersion == 9 && minorVersion < 3) {
        //    throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("PostgreSQL", "PostgreSQL", version);
        //}
        // [/enterprise-not]
        if (majorVersion > 10) {
            recommendFlywayUpgrade("PostgreSQL", version);
        }
    }

    public String getDbName() {
        return "postgresql";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return mainConnection.getJdbcTemplate().queryForString("SELECT current_user");
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

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new PostgreSQLSqlStatementBuilder(getDefaultDelimiter());
    }

    @Override
    public String doQuote(String identifier) {
        return pgQuote(identifier);
    }

    static String pgQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
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
