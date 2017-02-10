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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MongoMetaDataTable;

import com.mongodb.MongoClient;

/**
 * Handles Flyway's baseline command inside of MongoDB.
 */
public class MongoBaseline implements Baseline {
	private static final Log LOG = LogFactory.getLog(MongoBaseline.class);
	
	/** The MongoDB client used for accessing the metadata table. */
	private final MongoClient client;

	/** The metadata table. */
	private final MongoMetaDataTable metaDataTable;

	/** The version to tag an existing db with when baselining. */
	private final MigrationVersion baselineVersion;

	/** The description to tag the baseline migration with. */
	private final String baselineDescription;

	/** This is a list of callbacks that fire before or after the baseline task is executed. */
	private final MongoFlywayCallback[] callbacks;

	/**
	 * Creates a new MongoBaseline.
	 *
	 * @param client              The {@link MongoClient} used for accessing the metadata table.
	 * @param metaDataTable       The metadata table.
	 * @param baselineVersion     The version to tag an existing MongoDB with when baselining.
	 * @param baselineDescription The description to tag the baseline migration with.
	 * @param callbacks           The list of callbacks that fire off before and after a baseline task executes.
	 */
	public MongoBaseline(MongoClient client, MongoMetaDataTable metaDataTable, MigrationVersion baselineVersion,
						 String baselineDescription, MongoFlywayCallback[] callbacks) {
		this.client = client;
		this.metaDataTable = metaDataTable;
		this.baselineVersion = baselineVersion;
		this.baselineDescription = baselineDescription;
		this.callbacks = callbacks;
	}
	
	@Override
	public void baseline() {
		for (final MongoFlywayCallback callback : callbacks) {
			callback.beforeBaseline(client);
		}
			
		if (metaDataTable.hasAppliedMigrations()) {
			throw new FlywayException("Unable to baseline metadata table " + metaDataTable +
					" as it already contains migrations");
		}

		baselineTransaction();

		LOG.info("Successfully baselined schema with version: " + baselineVersion);

		for (final MongoFlywayCallback callback : callbacks) {
			callback.afterBaseline(client);
		}
	}

	private void baselineTransaction() throws FlywayException {
		if (metaDataTable.hasBaselineMarker()) {
			AppliedMigration baselineMarker = metaDataTable.getBaselineMarker();

			if (baselineVersion.equals(baselineMarker.getVersion()) &&
					baselineDescription.equals(baselineMarker.getDescription())) {
				LOG.info("Metadata table " + metaDataTable + " already initialized with (" +
						 baselineVersion + "," + baselineDescription + "). Skipping.");
				return;
			}

			throw new FlywayException("Unable to baseline metadata table " + metaDataTable +
					" with (" + baselineVersion + "," + baselineDescription +
					") as it has already been initialized with (" + baselineMarker.getVersion() +
					"," + baselineMarker.getDescription() + ")");
		}

		if (baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
			throw new FlywayException("Unable to baseline metadata table " + metaDataTable +
					" with version 0 as this version was used for schema creation");
		}

		metaDataTable.addBaselineMarker(baselineVersion, baselineDescription);
	}
}
