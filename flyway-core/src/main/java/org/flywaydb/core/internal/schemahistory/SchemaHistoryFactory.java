package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;

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
    public static SchemaHistory getSchemaHistory(FlywayConfiguration configuration, DbSupport dbSupport, Schema schema) {
        return new JdbcTableSchemaHistory(dbSupport, schema.getTable(configuration.getTable()),
                configuration.getInstalledBy() == null ? dbSupport.getCurrentUser() : configuration.getInstalledBy());
    }
}
