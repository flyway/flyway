/**
 * Copyright 2010-2015 Axel Fontaine
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

import java.sql.Connection;

/**
 * This is the main callback interface that should be implemented to get access to flyway lifecycle notifications.
 * Simply add code to the callback method you are interested in having.
 *
 * <p>Each callback method will run within its own transaction.</p>
 * 
 * @author Dan Bunker
 */
public interface FlywayCallback {
	/**
	 * Runs before the clean task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeClean(Connection connection);

	/**
	 * Runs after the clean task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterClean(Connection connection);

	/**
	 * Runs before the migrate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeMigrate(Connection connection);

	/**
	 * Runs after the migrate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterMigrate(Connection connection);

	/**
	 * Runs before each migration script is executed.
	 * 
	 * @param connection A valid connection to the database.
	 * @param info The current MigrationInfo for this migration.
	 */
	void beforeEachMigrate(Connection connection, MigrationInfo info);

	/**
	 * Runs after each migration script is executed.
	 * 
	 * @param connection A valid connection to the database.
	 * @param info The current MigrationInfo for this migration.
	 */
	void afterEachMigrate(Connection connection, MigrationInfo info);

	/**
	 * Runs before the validate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeValidate(Connection connection);

	/**
	 * Runs after the validate task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterValidate(Connection connection);

	/**
	 * Runs before the baseline task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeBaseline(Connection connection);

	/**
	 * Runs after the baseline task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterBaseline(Connection connection);

	/**
	 * Runs before the baseline task executes.
	 *
	 * @param connection A valid connection to the database.
	 * @deprecated Will be removed in Flyway 4.0. Use beforeBaseline() instead.
	 */
	@Deprecated
	void beforeInit(Connection connection);

	/**
	 * Runs after the baseline task executes.
	 *
	 * @param connection A valid connection to the database.
	 * @deprecated Will be removed in Flyway 4.0. Use afterBaseline() instead.
	 */
	@Deprecated
	void afterInit(Connection connection);

	/**
	 * Runs before the repair task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeRepair(Connection connection);

	/**
	 * Runs after the repair task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterRepair(Connection connection);

	/**
	 * Runs before the info task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void beforeInfo(Connection connection);

	/**
	 * Runs after the info task executes.
	 * 
	 * @param connection A valid connection to the database.
	 */
	void afterInfo(Connection connection);
}
