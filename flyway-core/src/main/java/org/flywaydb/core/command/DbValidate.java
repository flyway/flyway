/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.command;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.info.MigrationInfoServiceImpl;
import org.flywaydb.core.metadatatable.MetaDataTable;
import org.flywaydb.core.resolver.MigrationResolver;
import org.flywaydb.core.util.Pair;
import org.flywaydb.core.util.StopWatch;
import org.flywaydb.core.util.TimeFormat;
import org.flywaydb.core.util.jdbc.TransactionCallback;
import org.flywaydb.core.util.jdbc.TransactionTemplate;
import org.flywaydb.core.util.logging.Log;
import org.flywaydb.core.util.logging.LogFactory;

import java.sql.Connection;

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
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The connection to use.
     */
    private final Connection connectionMetaDataTable;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private boolean outOfOrder;

    /**
     * Creates a new database validator.
     *
     * @param connectionMetaDataTable The connection to use.
     * @param metaDataTable           The database metadata table.
     * @param migrationResolver       The migration resolver.
     * @param target                  The target version of the migration.
     * @param outOfOrder              Allows migrations to be run "out of order".
     */
    public DbValidate(Connection connectionMetaDataTable,
                      MetaDataTable metaDataTable, MigrationResolver migrationResolver,
                      MigrationVersion target, boolean outOfOrder) {
        this.connectionMetaDataTable = connectionMetaDataTable;
        this.metaDataTable = metaDataTable;
        this.migrationResolver = migrationResolver;
        this.target = target;
        this.outOfOrder = outOfOrder;
    }

    /**
     * Starts the actual migration.
     *
     * @return The validation error, if any.
     */
    public String validate() {
        LOG.debug("Validating migrations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Pair<Integer, String> result = new TransactionTemplate(connectionMetaDataTable).execute(new TransactionCallback<Pair<Integer, String>>() {
            public Pair<Integer, String> doInTransaction() {
                MigrationInfoServiceImpl migrationInfoService =
                        new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder);

                migrationInfoService.refresh();

                if (migrationInfoService.applied().length == 0) {
                    LOG.info("No migrations applied yet. No validation necessary.");
                    return Pair.of(0, null);
                }

                int count = migrationInfoService.all().length;
                String validationError = migrationInfoService.validate();
                return Pair.of(count, validationError);
            }
        });

        stopWatch.stop();

        int count = result.getLeft();
        if (count == 1) {
            LOG.info(String.format("Validated 1 migration (execution time %s)",
                    TimeFormat.format(stopWatch.getTotalTimeMillis())));
        } else {
            LOG.info(String.format("Validated %d migrations (execution time %s)",
                    count, TimeFormat.format(stopWatch.getTotalTimeMillis())));
        }

        return result.getRight();
    }
}
