/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.migration.init.InitMigration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Main workflow for migrating the database.
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
     * @param version     The version to initialize the metadata table with.
     * @param description The description for the ionitial version.
     *
     * @throws InitException when the initialization failed.
     */
    public void init(SchemaVersion version, String description) throws InitException {
        if (metaDataTable.getCurrentSchemaVersion() != SchemaVersion.EMPTY) {
            throw new InitException(
                    "Schema already initialized. Current Version: " + metaDataTable.getCurrentSchemaVersion());
        }

        metaDataTable.createIfNotExists();

        final Migration initialMigration = new InitMigration(version, description);

        final MetaDataTableRow metaDataTableRow = new MetaDataTableRow(initialMigration);
        metaDataTableRow.update(0, MigrationState.SUCCESS);

        try {
            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Void doInTransaction(TransactionStatus status) {
                    metaDataTable.insert(metaDataTableRow);
                    return null;
                }
            });
        } catch (TransactionException e) {
            throw new InitException("Unable to initialize schema", e);
        }

        LOG.info("Schema initialized with version: " + metaDataTableRow.getVersion());
    }
}
