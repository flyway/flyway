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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * SQL Server connection.
 */
public class SQLServerConnection extends Connection<SQLServerDatabase> {
    private static final Log LOG = LogFactory.getLog(SQLServerConnection.class);

    private final String originalDatabaseName;
    private final String originalAnsiNulls;

    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    SQLServerConnection(FlywayConfiguration configuration, SQLServerDatabase database, java.sql.Connection connection, int nullType
                        // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                        // [/pro]
    ) {
        super(configuration, database, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
        try {
            originalDatabaseName = jdbcTemplate.queryForString("SELECT DB_NAME()");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine current database", e);
        }
        try {
            originalAnsiNulls = database.isAzure() ? null :
                    jdbcTemplate.queryForString("DECLARE @ANSI_NULLS VARCHAR(3) = 'OFF';\n" +
                    "IF ( (32 & @@OPTIONS) = 32 ) SET @ANSI_NULLS = 'ON';\n" +
                    "SELECT @ANSI_NULLS AS ANSI_NULLS;");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine ANSI NULLS state", e);
        }
    }

    public void setCurrentDatabase(String databaseName) throws SQLException {
        jdbcTemplate.execute("USE " + database.quote(databaseName));
    }


    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT SCHEMA_NAME()");
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        // Always restore original database and connection state in case they were changed in a previous migration or callback.
        setCurrentDatabase(originalDatabaseName);
        if (!database.isAzure()) {
            jdbcTemplate.execute("SET ANSI_NULLS " + originalAnsiNulls);
        }

        if (!schemaMessagePrinted) {
            LOG.info("SQLServer does not support setting the schema for the current session. Default schema NOT changed to " + schema);
            // Not currently supported.
            // See http://connect.microsoft.com/SQLServer/feedback/details/390528/t-sql-statement-for-changing-default-schema-context
            schemaMessagePrinted = true;
        }
    }

    @Override
    public Schema getSchema(String name) {
        return new SQLServerSchema(jdbcTemplate, database, originalDatabaseName, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return new SQLServerApplicationLockTemplate(this, jdbcTemplate, originalDatabaseName, table.toString().hashCode()).execute(callable);
    }
}
