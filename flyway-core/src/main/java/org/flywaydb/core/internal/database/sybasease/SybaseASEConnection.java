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
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;

/**
 * Sybase ASE Connection.
 */
public class SybaseASEConnection extends Connection<SybaseASEDatabase> {
    private static final Log LOG = LogFactory.getLog(SybaseASEConnection.class);

    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    SybaseASEConnection(FlywayConfiguration configuration, SybaseASEDatabase database, java.sql.Connection connection, int nullType
                        // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                        // [/pro]
    ) {
        super(configuration, database, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }


    @Override
    public Schema getSchema(String name) {
        //Sybase does not support schemas, nor changing users on the fly. Always return the same dummy schema.
        return new SybaseASESchema(jdbcTemplate, database, "dbo");
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return "dbo";
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!schemaMessagePrinted) {
            LOG.info("Sybase ASE does not support setting the schema for the current session. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }
}
