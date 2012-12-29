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
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles Flyway's automatic schema creation.
 */
public class DbSchemas {
    private static final Log LOG = LogFactory.getLog(DbSchemas.class);

    /**
     * The database connection to use for accessing the metadata table.
     */
    private final Connection connection;

    /**
     * The schemas managed by Flyway.
     */
    private final Schema[] schemas;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * Creates a new DbSchemas.
     *
     * @param connection    The database connection to use for accessing the metadata table.
     * @param schemas       The schemas managed by Flyway.
     * @param metaDataTable The metadata table.
     */
    public DbSchemas(Connection connection, Schema[] schemas, MetaDataTable metaDataTable) {
        this.connection = connection;
        this.schemas = schemas;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Creates the schemas
     */
    public void create() {
        try {
            new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
                public Void doInTransaction() {
                    for (Schema schema : schemas) {
                        if (schema.exists()) {
                            LOG.debug("Schema " + schema + " already exists. Skipping schema creation.");
                            return null;
                        }
                    }

                    for (Schema schema : schemas) {
                        try {
                            LOG.info("Creating schema " + schema + " ...");
                            schema.create();
                        } catch (SQLException e) {
                            throw new FlywayException("Failed create schema " + schema, e);
                        }
                    }

                    metaDataTable.addSchemasMarker(schemas);

                    return null;
                }
            });
        } catch (SQLException e) {
            throw new FlywayException("Error initializing metadata table " + metaDataTable, e);
        }
    }
}
