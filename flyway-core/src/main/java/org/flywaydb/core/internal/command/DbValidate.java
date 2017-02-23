/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Handles the validate command.
 *
 * @author Axel Fontaine
 */
public class DbValidate {
    private static final Log LOG = LogFactory.getLog(DbValidate.class);

    /**
     * The target version of the migration.
     */
    private final MigrationVersion target;

    /**
     * The database metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The schema containing the metadata table.
     */
    private final Schema schema;

    /**
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The connection to use.
     */
    private final Connection connection;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private final boolean outOfOrder;

    /**
     * Whether pending migrations are allowed.
     */
    private final boolean pending;

    /**
     * Whether missing migrations are allowed.
     */
    private final boolean missing;

    /**
     * Whether future migrations are allowed.
     */
    private final boolean future;

    /**
     * This is a list of callbacks that fire before or after the validate task is executed.
     * You can add as many callbacks as you want.  These should be set on the Flyway class
     * by the end user as Flyway will set them automatically for you here.
     */
    private final FlywayCallback[] callbacks;

    /**
     * The DB support for the connection.
     */
    private final DbSupport dbSupport;

    /**
     * Creates a new database validator.
     *
     * @param connection        The connection to use.
     * @param dbSupport         The DB support for the connection.
     * @param metaDataTable     The database metadata table.
     * @param schema            The database schema to use by default.
     * @param migrationResolver The migration resolver.
     * @param target            The target version of the migration.
     * @param outOfOrder        Allows migrations to be run "out of order".
     * @param pending           Whether pending migrations are allowed.
     * @param missing           Whether missing migrations are allowed.
     * @param future            Whether future migrations are allowed.
     * @param callbacks         The lifecycle callbacks.
     */
    public DbValidate(Connection connection,
                      DbSupport dbSupport, MetaDataTable metaDataTable, Schema schema, MigrationResolver migrationResolver,
                      MigrationVersion target, boolean outOfOrder, boolean pending, boolean missing, boolean future, FlywayCallback[] callbacks) {
        this.connection = connection;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.target = target;
        this.outOfOrder = outOfOrder;
        this.pending = pending;
        this.missing = missing;
        this.future = future;
        this.callbacks = callbacks;
    }

    /**
     * Starts the actual migration.
     *
     * @return The validation error, if any.
     */
    public String validate() {
        if (!schema.exists()) {
            if (!migrationResolver.resolveMigrations().isEmpty() && !pending) {
                return "Schema " + schema + " doesn't exist yet";
            }
            return null;
        }

        try {
            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupport.changeCurrentSchemaTo(schema);
                        callback.beforeValidate(connection);
                        return null;
                    }
                });
            }

            LOG.debug("Validating migrations ...");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Pair<Integer, String> result = new TransactionTemplate(connection).execute(new Callable<Pair<Integer, String>>() {
                @Override
                public Pair<Integer, String> call() {
                    dbSupport.changeCurrentSchemaTo(schema);
                    MigrationInfoServiceImpl migrationInfoService =
                            new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder, pending, missing, future);

                    migrationInfoService.refresh();

                    int count = migrationInfoService.all().length;
                    String validationError = migrationInfoService.validate();
                    return Pair.of(count, validationError);
                }
            });

            stopWatch.stop();

            String error = result.getRight();
            if (error == null) {
                int count = result.getLeft();
                if (count == 1) {
                    LOG.info(String.format("Successfully validated 1 migration (execution time %s)",
                            TimeFormat.format(stopWatch.getTotalTimeMillis())));
                } else {
                    LOG.info(String.format("Successfully validated %d migrations (execution time %s)",
                            count, TimeFormat.format(stopWatch.getTotalTimeMillis())));
                }
            }

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupport.changeCurrentSchemaTo(schema);
                        callback.afterValidate(connection);
                        return null;
                    }
                });
            }

            return error;
        } finally {
            dbSupport.restoreCurrentSchema();
        }
    }
}
