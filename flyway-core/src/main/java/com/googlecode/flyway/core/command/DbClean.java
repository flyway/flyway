/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.command;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main workflow for cleaning the database.
 */
public class DbClean {
    private static final Log LOG = LogFactory.getLog(DbClean.class);

    /**
     * The connection to use.
     */
    private final Connection connection;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The schemas to clean.
     */
    private final Schema[] schemas;

    /**
     * Creates a new database cleaner.
     *
     * @param connection    The connection to use.
     * @param metaDataTable The metadata table.
     * @param schemas       The schemas to clean.
     */
    public DbClean(Connection connection, MetaDataTable metaDataTable, Schema[] schemas) {
        this.connection = connection;
        this.metaDataTable = metaDataTable;
        this.schemas = schemas;
    }

    /**
     * Cleans the schemas of all objects.
     *
     * @throws FlywayException when clean failed.
     */
    public void clean() throws FlywayException {
        boolean dropSchemas = false;
        try {
            dropSchemas = metaDataTable.hasSchemasMarker();
        } catch (Exception e) {
            LOG.error("Error while checking whether the schemas should be dropped", e);
        }

        for (Schema schema : schemas) {
            if (dropSchemas) {
                dropSchema(schema);
            } else {
                cleanSchema(schema);
            }
        }
    }

    /**
     * Drops this schema.
     *
     * @param schema The schema to drop.
     * @throws FlywayException when the drop failed.
     */
    private void dropSchema(final Schema schema) {
        LOG.debug("Dropping schema " + schema + " ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
                public Void doInTransaction() throws SQLException {
                    schema.drop();
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new FlywayException("Error while dropping schema " + schema, e);
        }
        stopWatch.stop();
        LOG.info(String.format("Dropped schema %s (execution time %s)",
                schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }

    /**
     * Cleans this schema of all objects.
     *
     * @param schema The schema to clean.
     * @throws FlywayException when clean failed.
     */
    private void cleanSchema(final Schema schema) {
        LOG.debug("Cleaning schema " + schema + " ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
                public Void doInTransaction() throws SQLException {
                    schema.clean();
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new FlywayException("Error while cleaning schema " + schema.getName(), e);
        }
        stopWatch.stop();
        LOG.info(String.format("Cleaned schema %s (execution time %s)",
                schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }
}
