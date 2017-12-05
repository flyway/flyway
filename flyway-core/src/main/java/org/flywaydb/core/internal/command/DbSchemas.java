/*
 * Copyright 2010-2017 Boxfuse GmbH
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

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

import java.util.concurrent.Callable;

/**
 * Handles Flyway's automatic schema creation.
 */
public class DbSchemas {
    private static final Log LOG = LogFactory.getLog(DbSchemas.class);

    /**
     * The database connection to use for accessing the schema history table.
     */
    private final Connection connection;

    /**
     * The schemas managed by Flyway.
     */
    private final Schema[] schemas;

    /**
     * The schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * Creates a new DbSchemas.
     *
     * @param database      The database to use.
     * @param schemas       The schemas managed by Flyway.
     * @param schemaHistory The schema history table.
     */
    public DbSchemas(Database database, Schema[] schemas, SchemaHistory schemaHistory) {
        this.connection = database.getMainConnection();
        this.schemas = schemas;
        this.schemaHistory = schemaHistory;
    }

    /**
     * Creates the schemas
     */
    public void create() {
        int retries = 0;
        while (true) {
            try {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Void call() {
                        for (Schema schema : schemas) {
                            if (schema.exists()) {
                                LOG.debug("Schema " + schema + " already exists. Skipping schema creation.");
                                return null;
                            }
                        }

                        for (Schema schema : schemas) {
                            LOG.info("Creating schema " + schema + " ...");
                            schema.create();
                        }

                        schemaHistory.create();
                        schemaHistory.addSchemasMarker(schemas);

                        return null;
                    }
                });
                return;
            } catch (RuntimeException e) {
                if (++retries >= 10) {
                    throw e;
                }
                try {
                    LOG.debug("Schema creation failed. Retrying in 1 sec ...");
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // Ignore
                }
            }
        }
    }
}
