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

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.MongoMetaDataTable;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import com.mongodb.MongoClient;

/**
 * Handles the validate command for a Mongo database.
 */
public class MongoValidate implements Validate {
	private static final Log LOG = LogFactory.getLog(MongoValidate.class);

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
	 * The connection to use.
	 */
	private final MongoClient client;

	/**
	 * Allows migrations to be run "out of order".
	 * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
	 * it will be applied too instead of being ignored.</p>
	 * <p>(default: {@code false})</p>
	 */
	private final boolean outOfOrder;

	/**
	 * Whether pending migrations are allowed.
	 */
	private final boolean pending;

	/**
	 * Whether future migrations are allowed.
	 */
	private final boolean future;

	/**
	 * This is a list of callbacks that fire before or after the validate task is executed.
	 * You can add as many callbacks as you want.  These should be set on the Flyway class
	 * by the end user as Flyway will set them automatically for you here.
	 */
	private final MongoFlywayCallback[] callbacks;

	/**
	 * Creates a new database validator.
	 *
	 * @param target            The target version of the migration.
	 * @param metaDataTable     The database metadata table.
	 * @param migrationResolver The migration resolver.
	 * @param client        The client to use for interacting with Mongo.
	 * @param outOfOrder        Allows migrations to be run "out of order".
	 * @param pending           Whether pending migrations are allowed.
	 * @param future            Whether future migrations are allowed.
	 * @param callbacks         The lifecycle callbacks.
	 */
	public MongoValidate(MongoClient client, MongoMetaDataTable metaDataTable,
						 MigrationResolver migrationResolver, MigrationVersion target,
						 boolean outOfOrder, boolean pending, boolean future, MongoFlywayCallback[] callbacks) {
		this.target = target;
		this.metaDataTable = metaDataTable;
		this.migrationResolver = migrationResolver;
		this.client = client;
		this.outOfOrder = outOfOrder;
		this.pending = pending;
		this.future = future;
		this.callbacks = callbacks;
	}

	@Override
	public String validate() {
		for (final MongoFlywayCallback callback : callbacks) {
			callback.beforeValidate(client);
		}

		LOG.debug("Validating migrations ...");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Pair<Integer, String> result = validationDisjunction();
	
		stopWatch.stop();

		String error = result.getRight();
		if (error == null) {
			int count = result.getLeft();
			if (count == 1) {
				LOG.info(String.format("Successfully validated 1 migration (execution time %s)",
						 TimeFormat.format(stopWatch.getTotalTimeMillis())));
			} else {
				LOG.info(String.format("Successfully validated %d migrations (execution time %s)",
					 	count, TimeFormat.format(stopWatch.getTotalTimeMillis())));
			}
		}

		for (final MongoFlywayCallback callback : callbacks) {
			callback.afterValidate(client);
		}

		return error;
	}

	private Pair<Integer, String> validationDisjunction() {
		MigrationInfoServiceImpl migrationInfoService =
				new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder, pending, future);
		
		migrationInfoService.refresh();
		
		int count = migrationInfoService.all().length;
		String validationError = migrationInfoService.validate();
		
		return Pair.of(count, validationError);
	}
}
