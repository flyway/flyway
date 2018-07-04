package org.flywaydb.core.internal.database.cache;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.Types;

/**
 * Holds a Cache connection
 */
public class CacheConnection extends Connection<CacheDatabase> {

    CacheConnection(Configuration configuration, CacheDatabase database, java.sql.Connection connection, boolean originalAutoCommit) {
        super(configuration, database, connection, originalAutoCommit, Types.VARCHAR);
    }

    @Override
    public Schema getSchema(String name) {
        return new CacheSchema(jdbcTemplate, database, name);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "SQLUser"; //Schema by default
    }

}
