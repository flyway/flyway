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
package org.flywaydb.core.api.configuration;

import org.flywaydb.core.api.callback.MongoFlywayCallback;

import com.mongodb.MongoClient;

/**
 * Interface for mongo flyway configuration. Can be used to provide configuration data to migrations
 * and callbacks.
 */
public interface MongoFlywayConfiguration extends FlywayConfiguration {

	/**
	 * Retrieves the name of the database that a Mongo client is connected to.
	 *
	 * @return The name of the database a Mongo client is connected to.
	 */
	String getDatabaseName();
	
	/**
	 * Retrieves the MongoClient to use to access the database. Must have the necessary privileges
	 * to execute transactions.
	 *
	 * @return The mongoClient to use to access the database. Must have the necessary privileges to
	 * execute transactions.
	 */
	MongoClient getMongoClient();

	/**
	 * Gets the callbacks for MongoDB lifecycle notifications.
	 *
	 * @return The callbacks for MongoDB lifecycle notifications. An empty array if none. (default: none)
	 */
	MongoFlywayCallback[] getMongoCallbacks();

	/**
	 * Retrieves the file name suffix for mongo migrations.
	 * <p/>
	 * <p>Mongo migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.js</p>
	 *
	 * @return The file name suffix for mongo migrations. (default: .js)
	 */
	String getMongoMigrationSuffix();

	/**
	 * Retrieves the file name prefix for repeatable mongo migrations.
	 * <p/>
	 * <p>Repeatable mongo migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to R__My_description.js</p>
	 *
	 * @return The file name prefix for repeatable sql migrations. (default: R)
	 */
	String getRepeatableMongoMigrationPrefix();

	/**
	 * Retrieves the file name separator for mongo migrations.
	 * <p/>
	 * <p>Mongo migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.js</p>
	 *
	 * @return The file name separator for mongo migrations. (default: __)
	 */
	String getMongoMigrationSeparator();

	/**
	 * Retrieves the file name prefix for mongo migrations.
	 * <p/>
	 * <p>Mongo migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
	 * which using the defaults translates to V1_1__My_description.js</p>
	 *
	 * @return The file name prefix for mongo migrations. (default: V)
	 */
	String getMongoMigrationPrefix();

}
