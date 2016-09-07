/**
 * Copyright 2010-2016 Boxfuse GmbH
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
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.resolver.AbstractMongoMigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.FlywayMigrationExecutor;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MongoMetaDataTable;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * Main workflow for migrating the Mongo database.
 */
public class MongoMigrate implements Migrate {
	private static final Log LOG = LogFactory.getLog(MongoMigrate.class);

	/**
	 * The target version of the migration.
	 */
	private final MigrationVersion target;

	/**
	 * The database metadata table.
	 */
	private final MongoMetaDataTable metaDataTable;

	/**
	 * The migration resolver.
	 */
	private final MigrationResolver migrationResolver;

	/**
	 * The Mongo client to use for interacting with the database.
	 */
	private final MongoClient client;

	/**
	 * Flag whether to ignore future migrations or not.
	 */
	private final boolean ignoreFutureMigrations;

	/**
	 * Flag whether to ignore failed future migrations or not.
	 */
	private final boolean ignoreFailedFutureMigration;

	/**
	 * Allows migrations to be run "out of order".
	 * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
	 * it will be applied too instead of being ignored.</p>
	 * <p>(default: {@code false})</p>
	 */
	private final boolean outOfOrder;

	/**
	 * This is a list of callbacks that fire before or after the migrate task is executed.
	 * You can add as many callbacks as you want.  These should be set on the Flyway class
	 * by the end user as Flyway will set them automatically for you here.
	 */
	private final MongoFlywayCallback[] callbacks;

	/**
	 * Creates a new database migrator.
	 *
	 * @param client     The Mongo client to use for interacting with the database.
	 * @param metaDataTable               The database metadata table.
	 * @param migrationResolver           The migration resolver.
	 * @param target                      The target version of the migration.
	 * @param ignoreFutureMigrations      Flag whether to ignore future migrations or not.
	 * @param ignoreFailedFutureMigration Flag whether to ignore failed future migrations or not.
	 * @param outOfOrder                  Allows migrations to be run "out of order".
	 */
	public MongoMigrate(MongoClient client, MongoMetaDataTable metaDataTable, MigrationResolver migrationResolver,
									 MigrationVersion target, boolean ignoreFutureMigrations, boolean ignoreFailedFutureMigration,
									 boolean outOfOrder, MongoFlywayCallback[] callbacks) {
		this.client = client;
		this.metaDataTable = metaDataTable;
		this.migrationResolver = migrationResolver;
		this.target = target;
		this.ignoreFutureMigrations = ignoreFutureMigrations;
		this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
		this.outOfOrder = outOfOrder;
		this.callbacks = callbacks;
	}

	@Override
	public int migrate() throws FlywayException {
		for (final MongoFlywayCallback callback : callbacks) {
			callback.beforeMigrate(client);
		}

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int migrationSuccessCount = 0;
		while (true) {
			final boolean firstRun = migrationSuccessCount == 0;
			boolean done = migrationRunner(firstRun);

			if (done) {
				// No further migrations available
				break;
			}

			migrationSuccessCount++;
		}

		stopWatch.stop();

		logSummary(migrationSuccessCount, stopWatch.getTotalTimeMillis());

		for (final MongoFlywayCallback callback : callbacks) {
			callback.afterMigrate(client);
		}

		return migrationSuccessCount;
	}

	/**
	 * Logs the summary of this migration run.
	 *
	 * @param migrationSuccessCount The number of successfully applied migrations.
	 * @param executionTime         The total time taken to perform this migration run (in ms).
	 */
	private void logSummary(int migrationSuccessCount, long executionTime) {
		if (migrationSuccessCount == 0) {
			LOG.info("Mongo is up to date. No migration necessary.");
			return;
		}

		if (migrationSuccessCount == 1) {
			LOG.info("Successfully applied 1 migration to MongoDB (execution time " + TimeFormat.format(executionTime) + ").");
		} else {
			LOG.info("Successfully applied " + migrationSuccessCount + " migrations to MongoDB (execution time " + TimeFormat.format(executionTime) + ").");
		}
	}

	private boolean migrationRunner(boolean firstRun) {
		metaDataTable.lock();

		MigrationInfoServiceImpl infoService =
			new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder, true, true);
		infoService.refresh();

		MigrationVersion currentDbVersion = MigrationVersion.EMPTY;
		if (infoService.current() != null) {
			currentDbVersion = infoService.current().getVersion();
		}
		if (firstRun) {
			LOG.info("Current version of MongoDB: " + currentDbVersion);

			if (outOfOrder) {
				LOG.warn("outOfOrder mode is active. Migration of MongoDB may not be reproducible.");
			}
		}

		MigrationInfo[] future = infoService.future();
		if (future.length > 0) {
			MigrationInfo[] resolved = infoService.resolved();
			if (resolved.length == 0) {
				LOG.warn("MongoDB has version " + currentDbVersion
								 + ", but no migration could be resolved in the configured locations !");
			} else {
				int offset = resolved.length - 1;
				while (resolved[offset].getVersion() == null) {
					// Skip repeatable migrations
					offset--;
				}
				LOG.warn("MongoDB has a version (" + currentDbVersion
								 + ") that is newer than the latest available migration ("
								 + resolved[offset].getVersion() + ") !");
			}
		}

		MigrationInfo[] failed = infoService.failed();
		if (failed.length > 0) {
			if ((failed.length == 1)
					&& (failed[0].getState() == MigrationState.FUTURE_FAILED)
					&& (ignoreFutureMigrations || ignoreFailedFutureMigration)) {
				LOG.warn("MongoDB contains a failed future migration to version " + failed[0].getVersion() + " !");
			} else {
				throw new FlywayException("Mongo contains a failed migration to version " + failed[0].getVersion() + " !");
			}
		}

		MigrationInfoImpl[] pendingMigrations = infoService.pending();

		if (pendingMigrations.length == 0) {
			return true;
		}

		boolean isOutOfOrder = pendingMigrations[0].getVersion() != null
			&& pendingMigrations[0].getVersion().compareTo(currentDbVersion) < 0;
		return applyMigration(pendingMigrations[0], isOutOfOrder);
	}
	
	/**
	 * Applies this migration to the database. The migration state and the execution time are updated accordingly.
	 *
	 * @param migration    The migration to apply.
	 * @param isOutOfOrder If this migration is being applied out of order.
	 * @return The result of the migration.
	 */
	private Boolean applyMigration(final MigrationInfoImpl migration, boolean isOutOfOrder) {
		MigrationVersion version = migration.getVersion();
		final String migrationText;
		if (version != null) {
			migrationText = "Mongo to version " + version + " - " + migration.getDescription() +
				(isOutOfOrder ? " (out of order)" : "");
		} else {
			migrationText = "Mongo with repeatable migration " + migration.getDescription();
		}
		LOG.info("Migrating " + migrationText);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		try {
			final AbstractMongoMigrationExecutor migrationExecutor =
				(AbstractMongoMigrationExecutor) migration.getResolvedMigration().getExecutor();
		  
			doMigrate(migration, migrationExecutor, migrationText);
		} catch (MongoException e) {
			String failedMsg = "Migration of " + migrationText + " failed!";
			LOG.error(failedMsg + " Please restore backups and roll back database and code!");

			stopWatch.stop();
			int executionTime = (int) stopWatch.getTotalTimeMillis();
			AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(), migration.getType(),
																															 migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, false);
			metaDataTable.addAppliedMigration(appliedMigration);
				
			throw new FlywayException("Could not apply migration " + appliedMigration.getVersion().getVersion(), e);
		}

		stopWatch.stop();
		int executionTime = (int) stopWatch.getTotalTimeMillis();
		AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(), migration.getType(),
																														 migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, true);
		metaDataTable.addAppliedMigration(appliedMigration);

		return false;
	}

	private void doMigrate(MigrationInfoImpl migration, FlywayMigrationExecutor migrationExecutor, String migrationText) {
		AbstractMongoMigrationExecutor mongoExecutor = (AbstractMongoMigrationExecutor) migrationExecutor;
			
		for (final MongoFlywayCallback callback : callbacks) {
			callback.beforeEachMigrate(client, migration);
		}

	  mongoExecutor.execute(client);
		LOG.debug("Successfully completed migration of " + migrationText);

		for (final MongoFlywayCallback callback : callbacks) {
			callback.afterEachMigrate(client, migration);
		}
	}
}
