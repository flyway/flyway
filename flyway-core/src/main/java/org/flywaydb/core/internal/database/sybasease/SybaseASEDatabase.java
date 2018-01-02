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
package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Sybase ASE database.
 */
public class SybaseASEDatabase extends Database<SybaseASEConnection> {
    /**
     * Creates a new Sybase ASE database.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The initial connection.
     * @param jconnect      Whether we are using the official jConnect driver or not (jTDS).
     */
    public SybaseASEDatabase(FlywayConfiguration configuration, Connection connection, boolean jconnect
                             // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                             // [/pro]
    ) {
        super(configuration, connection, jconnect ? Types.VARCHAR : Types.NULL
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }

    @Override
    protected SybaseASEConnection getConnection(Connection connection, int nullType
                                                // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                                                // [/pro]
    ) {
        return new SybaseASEConnection(configuration, this, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
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
    protected String doGetCurrentUser() throws SQLException {
        return mainConnection.getJdbcTemplate().queryForString("SELECT user_name()");
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
