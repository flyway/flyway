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
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;

/**
 * This is the main callback interface that should be implemented to get access to flyway lifecycle
 * notifications. Simply add code to the callback method you are interested in having. Some
 * convenience implementations with methods doing nothing is provided with {@link BaseSQLFlywayCallback}
 * and {@link BaseMongoFlywayCallback}. This interface cannot be implemented directly and instead,
 * classes must extend {@link SQLFlywayCallback} or {@link MongoFlywayCallback}.
 *
 * <p>Each callback method will run within its own transaction.</p>
 * 
 * @author Dan Bunker
 * @author Brennan Collins
 */
interface Callback<T> {
	/**
	 * Runs before the clean task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeClean(T connection);

	/**
	 * Runs after the clean task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterClean(T connection);

	/**
	 * Runs before the migrate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeMigrate(T connection);

	/**
	 * Runs after the migrate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterMigrate(T connection);

	/**
	 * Runs before each migration script is executed.
	 * 
	 * @param connection A valid connection to the database.
	 * @param info The current MigrationInfo for this migration.
	 */
	void beforeEachMigrate(T connection, MigrationInfo info);

	/**
	 * Runs after each migration script is executed.
	 * 
	 * @param connection A valid connection to the database.
	 * @param info The current MigrationInfo for this migration.
	 */
	void afterEachMigrate(T connection, MigrationInfo info);

	/**
	 * Runs before the validate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeValidate(T connection);

	/**
	 * Runs after the validate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterValidate(T connection);

	/**
	 * Runs before the baseline task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeBaseline(T connection);

	/**
	 * Runs after the baseline task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterBaseline(T connection);

	/**
	 * Runs before the repair task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeRepair(T connection);

	/**
	 * Runs after the repair task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterRepair(T connection);

	/**
	 * Runs before the info task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeInfo(T connection);

	/**
	 * Runs after the info task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterInfo(T connection);
}
