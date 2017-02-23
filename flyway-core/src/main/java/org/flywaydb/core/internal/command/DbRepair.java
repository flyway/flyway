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

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Handles Flyway's repair command.
 */
public class DbRepair {
    private static final Log LOG = LogFactory.getLog(DbRepair.class);

    /**
     * The database connection to use for accessing the metadata table.
     */
    private final Connection connection;

    /**
     * The migration infos.
     */
    private final MigrationInfoServiceImpl migrationInfoService;

    /**
     * The schema containing the metadata table.
     */
    private final Schema schema;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * This is a list of callbacks that fire before or after the repair task is executed.
     * You can add as many callbacks as you want.  These should be set on the Flyway class
     * by the end user as Flyway will set them automatically for you here.
     */
    private final FlywayCallback[] callbacks;

    /**
     * The database-specific support.
     */
    private final DbSupport dbSupport;

    /**
     * Creates a new DbRepair.
     *
     * @param dbSupport         The database-specific support.
     * @param connection        The database connection to use for accessing the metadata table.
     * @param schema            The database schema to use by default.
     * @param migrationResolver The migration resolver.
     * @param metaDataTable     The metadata table.
     * @param callbacks         Callbacks for the Flyway lifecycle.
     */
    public DbRepair(DbSupport dbSupport, Connection connection, Schema schema, MigrationResolver migrationResolver, MetaDataTable metaDataTable, FlywayCallback[] callbacks) {
        this.dbSupport = dbSupport;
        this.connection = connection;
        this.schema = schema;
        this.migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, metaDataTable, MigrationVersion.LATEST, true, true, true, true);
        this.metaDataTable = metaDataTable;
        this.callbacks = callbacks;
    }

    /**
     * Repairs the metadata table.
     */
    public void repair() {
        try {
            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupport.changeCurrentSchemaTo(schema);
                        callback.beforeRepair(connection);
                        return null;
                    }
                });
            }

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            new TransactionTemplate(connection).execute(new Callable<Object>() {
                public Void call() {
                    dbSupport.changeCurrentSchemaTo(schema);
                    metaDataTable.removeFailedMigrations();
                    repairChecksumsAndDescriptions();
                    return null;
                }
            });

            stopWatch.stop();

            LOG.info("Successfully repaired metadata table " + metaDataTable + " (execution time "
                    + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
            if (!dbSupport.supportsDdlTransactions()) {
                LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
            }

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupport.changeCurrentSchemaTo(schema);
                        callback.afterRepair(connection);
                        return null;
                    }
                });
            }
        } finally {
            dbSupport.restoreCurrentSchema();
        }
    }

    public void repairChecksumsAndDescriptions() {
        migrationInfoService.refresh();
        for (MigrationInfo migrationInfo : migrationInfoService.all()) {
            MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl) migrationInfo;

            ResolvedMigration resolved = migrationInfoImpl.getResolvedMigration();
            AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
            if (resolved != null && applied != null && resolved.getVersion() != null) {
                if (!ObjectUtils.nullSafeEquals(resolved.getChecksum(), applied.getChecksum())
                        || !ObjectUtils.nullSafeEquals(resolved.getDescription(), applied.getDescription())) {
                    metaDataTable.update(migrationInfoImpl.getVersion(), resolved.getDescription(), resolved.getChecksum());
                }
            }
        }
    }
}
