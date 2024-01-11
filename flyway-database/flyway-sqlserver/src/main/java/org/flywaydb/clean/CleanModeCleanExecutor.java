package org.flywaydb.clean;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.command.clean.CleanExecutor;
import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.PlainExecutionTemplate;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

import java.util.List;

public class CleanModeCleanExecutor extends CleanExecutor {
    private final String cleanMode;

    public CleanModeCleanExecutor(Connection connection, Database database, SchemaHistory schemaHistory, CallbackExecutor callbackExecutor, String cleanMode) {
        super(connection, database, schemaHistory, callbackExecutor);
        this.cleanMode = cleanMode;
    }

    public void clean(Schema defaultSchema, Schema[] schemas, CleanResult cleanResult, List<String> dropSchemas) {
        try {
            connection.changeCurrentSchemaTo(defaultSchema);
            clean(schemas, cleanResult, dropSchemas);
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_CLEAN_ERROR);
            throw e;
        }
    }

    @Override
    protected void doCleanSchema(Schema schema) {
        if (Mode.ALL.name().equalsIgnoreCase(cleanMode)) {
            new PlainExecutionTemplate(true).execute(() -> {
                schema.clean();
                return null;
            });
        } else {
            super.doCleanSchema(schema);
        }
    }
}