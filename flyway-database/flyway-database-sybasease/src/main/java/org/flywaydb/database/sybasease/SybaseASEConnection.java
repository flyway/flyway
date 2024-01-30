package org.flywaydb.database.sybasease;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

/**
 * Sybase ASE Connection.
 */
public class SybaseASEConnection extends Connection<SybaseASEDatabase> {
    SybaseASEConnection(SybaseASEDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        //Sybase does not support schemas, nor changing users on the fly. Always return the same dummy schema.
        return new SybaseASESchema(jdbcTemplate, database, "dbo");
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "dbo";
    }
}