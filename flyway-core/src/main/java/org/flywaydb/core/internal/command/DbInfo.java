package org.flywaydb.core.internal.command;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

@RequiredArgsConstructor
public class DbInfo {
    private final CompositeMigrationResolver migrationResolver;
    private final SchemaHistory schemaHistory;
    private final Configuration configuration;
    private final Database database;
    private final CallbackExecutor callbackExecutor;
    private final Schema[] schemas;

    public MigrationInfoService info() {

        callbackExecutor.onEvent(Event.BEFORE_INFO);

        MigrationInfoServiceImpl migrationInfoService;
        try {
            migrationInfoService =
                    new MigrationInfoServiceImpl(migrationResolver, schemaHistory, database, configuration,
                                                 configuration.getTarget(), configuration.isOutOfOrder(), ValidatePatternUtils.getIgnoreAllPattern(), configuration.getCherryPick());
            migrationInfoService.refresh();
            migrationInfoService.setAllSchemasEmpty(schemas);
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_INFO_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_INFO);

        return migrationInfoService;
    }
}