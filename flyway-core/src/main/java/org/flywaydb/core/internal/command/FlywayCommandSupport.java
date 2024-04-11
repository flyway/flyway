package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
/**
 * Support for Flyway commands.
 
 */
public class FlywayCommandSupport {
    private CompositeMigrationResolver migrationResolver;
    private SchemaHistory schemaHistory;
    private Configuration configuration;
    private Database database;
    private CallbackExecutor callbackExecutor;



    public CompositeMigrationResolver getMigrationResolver() {
        return migrationResolver;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public SchemaHistory getSchemaHistory() {
        return schemaHistory;
    }

    public Database getDatabase() {
        return database;
    }

    public CallbackExecutor getCallbackExecutor() {
        return callbackExecutor;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void setMigrationResolver(CompositeMigrationResolver migrationResolver) {
        this.migrationResolver = migrationResolver;
    }

    public void setSchemaHistory(SchemaHistory schemaHistory) {
        this.schemaHistory = schemaHistory;
    }

    public void setCallbackExecutor(CallbackExecutor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    public FlywayCommandSupport(CompositeMigrationResolver migrationResolver, SchemaHistory schemaHistory,
            Configuration configuration, Database database, CallbackExecutor callbackExecutor) {
        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.configuration = configuration;
        this.database = database;
        this.callbackExecutor = callbackExecutor;
    }
}
