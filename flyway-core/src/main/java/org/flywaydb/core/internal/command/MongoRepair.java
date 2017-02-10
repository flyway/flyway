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

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MongoMetaDataTable;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import com.mongodb.MongoClient;

/**
 * Handles Flyway's repair command for MongoDB.
 */
public class MongoRepair implements Repair {
  private static final Log LOG = LogFactory.getLog(MongoRepair.class);
	
  /**
   * Mongo client for interacting with the database.
   */
  private final MongoClient client;

  /**
   * The migration infos.
   */
  private final MigrationInfoServiceImpl migrationInfoService;

  /**
   * The metadata table.
   */
  private final MongoMetaDataTable metaDataTable;

  /**
   * This is a list of callbacks that fire before or after the repair task is executed.
   */
  private final MongoFlywayCallback[] callbacks;

  /**
   * Creates a new MongoRepair instance.
   *
   * @param client Mongo client for interacting with the database.
   * @param migrationResolver The resolver for migrations.
   * @param metaDataTable The Mongo metadata table.
   * @param callbacks Callbacks for the Flyway lifecycle.
   */
  public MongoRepair(MongoClient client,
                     MigrationResolver migrationResolver,
                     MongoMetaDataTable metaDataTable,
                     MongoFlywayCallback[] callbacks) {
    this.client = client;
    this.migrationInfoService = new MigrationInfoServiceImpl(
      migrationResolver, metaDataTable, MigrationVersion.LATEST, true, true, true, true);
    this.metaDataTable = metaDataTable;
    this.callbacks = callbacks;
  }
	
	@Override
	public void repair() {
		for (final MongoFlywayCallback callback : callbacks) {
			callback.beforeRepair(client);
		}
		metaDataTable.removeFailedMigrations();
		repairChecksumsAndDescriptions();
		LOG.info("Successfully repaired Mongo metadata table " + metaDataTable.getCollectionName());
		
		for (final MongoFlywayCallback callback : callbacks) {
			callback.afterRepair(client);
		}
	}

	public void repairChecksumsAndDescriptions() {
		migrationInfoService.refresh();

		for (MigrationInfo migrationInfo : migrationInfoService.all()) {
			MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl) migrationInfo;

			ResolvedMigration resolved = migrationInfoImpl.getResolvedMigration();
			AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
			if ((resolved != null) && (applied != null)) {
				if (!ObjectUtils.nullSafeEquals(resolved.getChecksum(), applied.getChecksum())
						&& resolved.getVersion() != null) {
					metaDataTable.update(migrationInfoImpl.getVersion(), resolved.getDescription(), resolved.getChecksum());
				}
			}
		}
	}

}
