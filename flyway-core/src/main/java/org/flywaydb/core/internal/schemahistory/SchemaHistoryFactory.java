package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
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
     * @param dbSupport     The DbSupport object.
     * @param schema        The schema whose history to track.
     * @return The schema history.
     */
    public static SchemaHistory getSchemaHistory(FlywayConfiguration configuration, DbSupport dbSupport, Schema schema
                                                 // [pro]
            , DryRunStatementInterceptor dryRunStatementInterceptor
                                                 // [/pro]
    ) {
        String installedBy = configuration.getInstalledBy() == null
                ? dbSupport.getCurrentUser()
                : configuration.getInstalledBy();

        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory = new JdbcTableSchemaHistory(dbSupport, table, installedBy);

        // [pro]
        if (configuration.getDryRunOutput() != null) {
            dryRunStatementInterceptor.init(dbSupport, table);
            return new InMemorySchemaHistory(jdbcTableSchemaHistory.exists(),
                    jdbcTableSchemaHistory.allAppliedMigrations(), installedBy, dryRunStatementInterceptor);
        }
        // [/pro]

        return jdbcTableSchemaHistory;
    }
}
