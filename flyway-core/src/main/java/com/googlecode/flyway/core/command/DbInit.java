/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
import com.googlecode.flyway.core.metadatatable.AppliedMigration;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;

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
        new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                if (metaDataTable.hasAppliedMigrations()) {
                    throw new FlywayException("Unable to init metadata table " + metaDataTable + " as it already contains migrations");
                }
                if (metaDataTable.hasInitMarker()) {
                    AppliedMigration initMarker = metaDataTable.getInitMarker();
                    if (initVersion.equals(initMarker.getVersion())
                            && initDescription.equals(initMarker.getDescription())) {
                        LOG.info("Metadata table " + metaDataTable + " already initialized with ("
                                + initVersion + "," + initDescription + "). Skipping.");
                        return null;
                    }
                    throw new FlywayException("Unable to init metadata table " + metaDataTable + " with ("
                            + initVersion + "," + initDescription
                            + ") as it has already been initialized with ("
                            + initMarker.getVersion() + "," + initMarker.getDescription() + ")");
                }
                if (metaDataTable.hasSchemasMarker() && initVersion.equals(new MigrationVersion("0"))) {
                    throw new FlywayException("Unable to init metadata table " + metaDataTable + " with version 0 as this version was used for schema creation");
                }
                metaDataTable.addInitMarker(initVersion, initDescription);
                return null;
            }
        });

        LOG.info("Schema initialized with version: " + initVersion);
    }
}
