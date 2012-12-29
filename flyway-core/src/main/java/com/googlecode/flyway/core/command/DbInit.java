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
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles Flyway's init command.
 */
public class DbInit {
    private static final Log LOG = LogFactory.getLog(DbInit.class);

    /**
     * The database connection to use for accessing the metadata table.
     */
    private final Connection connection;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The version to tag an existing schema with when executing init.
     */
    private final MigrationVersion initVersion;

    /**
     * The description to tag an existing schema with when executing init.
     */
    private final String initDescription;

    /**
     * Creates a new DbInit.
     *
     * @param connection      The database connection to use for accessing the metadata table.
     * @param metaDataTable   The metadata table.
     * @param initVersion     The version to tag an existing schema with when executing init.
     * @param initDescription The description to tag an existing schema with when executing init.
     */
    public DbInit(Connection connection, MetaDataTable metaDataTable, MigrationVersion initVersion, String initDescription) {
        this.connection = connection;
        this.metaDataTable = metaDataTable;
        this.initVersion = initVersion;
        this.initDescription = initDescription;
    }

    /**
     * Initializes the database.
     */
    public void init() {
        try {
            new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
                public Void doInTransaction() {
                    if (metaDataTable.hasAppliedMigrations()) {
                        throw new FlywayException("Unable to init metadata table " + metaDataTable + " as it already contains migrations");
                    }
                    if (metaDataTable.hasInitMarker()) {
                        throw new FlywayException("Unable to init metadata table " + metaDataTable + " as it has already been initialized");
                    }
                    metaDataTable.init(initVersion, initDescription);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new FlywayException("Error initializing metadata table " + metaDataTable, e);

        }

        LOG.info("Schema initialized with version: " + initVersion);
    }
}
