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
package com.googlecode.flyway.core.init;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.migration.ResolvedMigration;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

/**
 * Workflow for initializing the database with a new metadata table and an initial marker version.
 *
 * @author Axel Fontaine
 */
public class DbInit {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbInit.class);

    /**
     * The database metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The transaction template to use.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * Creates a new database migrator.
     *
     * @param transactionTemplate The transaction template to use.
     * @param metaDataTable       The database metadata table.
     */
    public DbInit(TransactionTemplate transactionTemplate, MetaDataTable metaDataTable) {
        this.transactionTemplate = transactionTemplate;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Initializes the metadata table with this version and this description.
     *
     * @param initialVersion     The version to initialize the metadata table with.
     * @param initialDescription The description for the ionitial version.
     * @throws FlywayException when the initialization failed.
     */
    public void init(MigrationVersion initialVersion, String initialDescription) {
        if (metaDataTable.getCurrentSchemaVersion() != MigrationVersion.EMPTY) {
            throw new FlywayException(
                    "Schema already initialized. Current Version: " + metaDataTable.getCurrentSchemaVersion());
        }

        metaDataTable.createIfNotExists();

        final ResolvedMigration resolvedMigration = new ResolvedMigration();
        resolvedMigration.setVersion(initialVersion);
        resolvedMigration.setDescription(initialDescription);
        resolvedMigration.setScript(initialDescription);
        resolvedMigration.setType(MigrationType.INIT);

        transactionTemplate.execute(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                metaDataTable.insert(resolvedMigration, true, 0);
                return null;
            }
        });

        LOG.info("Schema initialized with version: " + initialVersion);
    }
}
