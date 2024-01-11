package org.flywaydb.database.sqlserver.synapse;

import org.flywaydb.database.sqlserver.SQLServerConnection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

import java.util.concurrent.Callable;

/**
 * Azure Synapse connection.
 */
public class SynapseConnection extends SQLServerConnection {

    SynapseConnection(SynapseDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        return new SynapseSchema(jdbcTemplate, database, originalDatabaseName, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return ExecutionTemplateFactory
                .createTableExclusiveExecutionTemplate(jdbcTemplate.getConnection(), table, database)
                .execute(callable);
    }

}