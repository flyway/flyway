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
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoClean implements Clean {
	private static final Log LOG = LogFactory.getLog(MongoClean.class);

	/**
	 * Whether to disable clean or not.
	 */
	private boolean cleanDisabled;

	/**
	 * The list of callbacks that fire before or after the clean task is executed.
	 */
	private final MongoFlywayCallback[] callbacks;
	
	/**
	 * The MongoDB client for interacting with the database.
	 */
	private final MongoClient client;

	/**
	 * Create a new MongoDB database cleaner.
	 *
	 * @param client The MongoDB client used to interact with the database.
	 * @param callbacks The list of callbacks to run before and after the "clean" command.
	 * @param cleanDisabled Whether or not to disable the "clean" command.
	 */
	public MongoClean(MongoClient client, MongoFlywayCallback[] callbacks, boolean cleanDisabled) {
		this.cleanDisabled = cleanDisabled;
		this.callbacks = callbacks;
		this.client = client;
	}

	@Override
	public void clean() throws FlywayException {
		if (cleanDisabled) {
			throw new FlywayException("Unable to execute clean as it has been disabled with the \"flyway.cleanDisabled\" property.");
		}
		
		for (final MongoFlywayCallback callback : callbacks) {
			callback.beforeClean(client);
		}

		for (String dbName : client.listDatabaseNames()) {
			MongoDatabase db = client.getDatabase(dbName);
			for (String collectionName : db.listCollectionNames()) {
				db.getCollection(collectionName).drop();
			}
		}
		LOG.info("Successfully cleaned Mongo.");
		
		for (final MongoFlywayCallback callback : callbacks) {
			callback.afterClean(client);
		}
	}
}
