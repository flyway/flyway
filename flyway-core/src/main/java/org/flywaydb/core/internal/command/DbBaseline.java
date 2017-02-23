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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Handles Flyway's baseline command.
 */
public class DbBaseline {
    private static final Log LOG = LogFactory.getLog(DbBaseline.class);

    /**
     * The database connection to use for accessing the metadata table.
     */
    private final Connection connection;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The version to tag an existing schema with when executing baseline.
     */
    private final MigrationVersion baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline.
     */
    private final String baselineDescription;

    /**
     * This is a list of callbacks that fire before or after the baseline task is executed.
     * You can add as many callbacks as you want.  These should be set on the Flyway class
     * by the end user as Flyway will set them automatically for you here.
     */
    private final FlywayCallback[] callbacks;

    /**
     * The DB support for the connection.
     */
    private final DbSupport dbSupport;

    /**
     * The schema containing the metadata table.
     */
    private final Schema schema;

    /**
     * Creates a new DbBaseline.
     *
     * @param connection          The database connection to use for accessing the metadata table.
     * @param dbSupport           The DB support for the connection.
     * @param metaDataTable       The database metadata table.
     * @param schema              The database schema to use by default.
     * @param baselineVersion     The version to tag an existing schema with when executing baseline.
     * @param baselineDescription The description to tag an existing schema with when executing baseline.
     */
    public DbBaseline(Connection connection, DbSupport dbSupport, MetaDataTable metaDataTable, Schema schema, MigrationVersion baselineVersion, String baselineDescription, FlywayCallback[] callbacks) {
        this.connection = connection;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.schema = schema;
        this.baselineVersion = baselineVersion;
        this.baselineDescription = baselineDescription;
        this.callbacks = callbacks;
    }

    /**
     * Baselines the database.
     */
    public void baseline() {
        try {
            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupport.changeCurrentSchemaTo(schema);
                        callback.beforeBaseline(connection);
                        return null;
                    }
                });
            }

            new TransactionTemplate(connection).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    dbSupport.changeCurrentSchemaTo(schema);
                    if (metaDataTable.hasBaselineMarker()) {
                        AppliedMigration baselineMarker = metaDataTable.getBaselineMarker();
                        if (baselineVersion.equals(baselineMarker.getVersion())
                                && baselineDescription.equals(baselineMarker.getDescription())) {
                            LOG.info("Metadata table " + metaDataTable + " already initialized with ("
                                    + baselineVersion + "," + baselineDescription + "). Skipping.");
                            return null;
                        }
                        throw new FlywayException("Unable to baseline metadata table " + metaDataTable + " with ("
                                + baselineVersion + "," + baselineDescription
                                + ") as it has already been initialized with ("
                                + baselineMarker.getVersion() + "," + baselineMarker.getDescription() + ")");
                    }
                    if (metaDataTable.hasSchemasMarker() && baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                        throw new FlywayException("Unable to baseline metadata table " + metaDataTable + " with version 0 as this version was used for schema creation");
                    }
                    if (metaDataTable.hasAppliedMigrations()) {
                        throw new FlywayException("Unable to baseline metadata table " + metaDataTable + " as it already contains migrations");
                    }
                    metaDataTable.addBaselineMarker(baselineVersion, baselineDescription);

                    return null;
                }
            });

            LOG.info("Successfully baselined schema with version: " + baselineVersion);

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupport.changeCurrentSchemaTo(schema);
                        callback.afterBaseline(connection);
                        return null;
                    }
                });
            }
        } finally {
            dbSupport.restoreCurrentSchema();
        }
    }
}
