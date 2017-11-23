package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * MySQL connection.
 */
public class MySQLConnection extends Connection<MySQLDatabase> {
    private static final Log LOG = LogFactory.getLog(MySQLConnection.class);

    MySQLConnection(FlywayConfiguration configuration, MySQLDatabase database, java.sql.Connection connection, int nullType
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
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.getConnection().getCatalog();
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!StringUtils.hasLength(schema)) {
            try {
                // Weird hack to switch back to no database selected...
                String newDb = database.quote(UUID.randomUUID().toString());
                jdbcTemplate.execute("CREATE SCHEMA " + newDb);
                jdbcTemplate.execute("USE " + newDb);
                jdbcTemplate.execute("DROP SCHEMA " + newDb);
            } catch (Exception e) {
                LOG.warn("Unable to restore connection to having no default schema: " + e.getMessage());
            }
        } else {
            jdbcTemplate.getConnection().setCatalog(schema);
        }
    }

    @Override
    public Schema getSchema(String name) {
        return new MySQLSchema(jdbcTemplate, database, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return new MySQLNamedLockTemplate(jdbcTemplate, table.toString().hashCode()).execute(callable);
    }
}
