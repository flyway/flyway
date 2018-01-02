/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Handles Flyway's baseline command.
 */
public class DbBaseline {
    private static final Log LOG = LogFactory.getLog(DbBaseline.class);

    /**
     * The database connection to use for accessing the schema history table.
     */
    private final Connection connection;

    /**
     * The schema history table.
     */
    private final SchemaHistory schemaHistory;

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
    private final List<FlywayCallback> callbacks;

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;

    /**
     * Creates a new DbBaseline.
     *
     * @param database           The database to use.
     * @param schemaHistory       The database schema history table.
     * @param schema              The database schema to use by default.
     * @param baselineVersion     The version to tag an existing schema with when executing baseline.
     * @param baselineDescription The description to tag an existing schema with when executing baseline.
     */
    public DbBaseline(Database database, SchemaHistory schemaHistory, Schema schema, MigrationVersion baselineVersion,
                      String baselineDescription, List<FlywayCallback> callbacks) {
        this.connection = database.getMainConnection();
        this.schemaHistory = schemaHistory;
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
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connection.changeCurrentSchemaTo(schema);
                        callback.beforeBaseline(connection.getJdbcConnection());
                        return null;
                    }
                });
            }

            schemaHistory.create();
            new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    connection.changeCurrentSchemaTo(schema);
                    if (schemaHistory.hasBaselineMarker()) {
                        AppliedMigration baselineMarker = schemaHistory.getBaselineMarker();
                        if (baselineVersion.equals(baselineMarker.getVersion())
                                && baselineDescription.equals(baselineMarker.getDescription())) {
                            LOG.info("Schema history table " + schemaHistory + " already initialized with ("
                                    + baselineVersion + "," + baselineDescription + "). Skipping.");
                            return null;
                        }
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with ("
                                + baselineVersion + "," + baselineDescription
                                + ") as it has already been initialized with ("
                                + baselineMarker.getVersion() + "," + baselineMarker.getDescription() + ")");
                    }
                    if (schemaHistory.hasSchemasMarker() && baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with version 0 as this version was used for schema creation");
                    }
                    if (schemaHistory.hasAppliedMigrations()) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already contains migrations");
                    }
                    schemaHistory.addBaselineMarker(baselineVersion, baselineDescription);

                    return null;
                }
            });

            LOG.info("Successfully baselined schema with version: " + baselineVersion);

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connection.changeCurrentSchemaTo(schema);
                        callback.afterBaseline(connection.getJdbcConnection());
                        return null;
                    }
                });
            }
        } finally {
            connection.restoreCurrentSchema();
        }
    }
}
