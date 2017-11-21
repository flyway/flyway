package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.schemahistory.pro.InMemorySchemaHistory;
import org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor;

/**
 * Factory to obtain a reference to the schema history.
 */
public class SchemaHistoryFactory {
    private SchemaHistoryFactory() {
        // Prevent instantiation
    }

    /**
     * Obtains a reference to the schema history.
     *
     * @param configuration The current Flyway configuration.
     * @param database     The Database object.
     * @param schema        The schema whose history to track.
     * @return The schema history.
     */
    public static SchemaHistory getSchemaHistory(FlywayConfiguration configuration, Database database, Schema schema
                                                 // [pro]
            , DryRunStatementInterceptor dryRunStatementInterceptor
                                                 // [/pro]
    ) {
        String installedBy = configuration.getInstalledBy() == null
                ? database.getCurrentUser()
                : configuration.getInstalledBy();

        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory = new JdbcTableSchemaHistory(database, table, installedBy);

        // [pro]
        if (configuration.getDryRunOutput() != null) {
            dryRunStatementInterceptor.init(database, table);
            return new InMemorySchemaHistory(jdbcTableSchemaHistory.exists(),
                    jdbcTableSchemaHistory.allAppliedMigrations(), installedBy, dryRunStatementInterceptor);
        }
        // [/pro]

        return jdbcTableSchemaHistory;
    }
}
