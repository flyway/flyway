/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.api;

import java.sql.Connection;

/**
 * This is the main callback interface that should be implemented to
 * get access to flyway lifecycle notifications.  Simply add code
 * or logic to the callback method you are interested in having
 * 
 * @author Dan Bunker
 */
public interface FlywayCallback {
	/**
	 * Runs before the clean task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void beforeClean(Connection dataConnection);

	/**
	 * Runs after the clean task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void afterClean(Connection dataConnection);

	/**
	 * Runs before the migrate task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void beforeMigrate(Connection dataConnection);

	/**
	 * Runs after the migrate task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void afterMigrate(Connection dataConnection);

	/**
	 * Runs before each migration script is executed
	 * 
	 * @param dataConnection A valid connection to the database
	 * @param info The current MigrationInfo for this migration
	 */
	void beforeEachMigrate(Connection dataConnection, MigrationInfo info);

	/**
	 * Runs after each migration script is executed
	 * 
	 * @param dataConnection A valid connection to the database
	 * @param info The current MigrationInfo for this migration
	 */
	void afterEachMigrate(Connection dataConnection, MigrationInfo info);

	/**
	 * Runs before the validate task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void beforeValidate(Connection dataConnection);

	/**
	 * Runs after the validate task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void afterValidate(Connection dataConnection);

	/**
	 * Runs before the init task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void beforeInit(Connection dataConnection);

	/**
	 * Runs after the init task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void afterInit(Connection dataConnection);

	/**
	 * Runs before the repair task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void beforeRepair(Connection dataConnection);

	/**
	 * Runs after the repair task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void afterRepair(Connection dataConnection);

	/**
	 * Runs before the info task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void beforeInfo(Connection dataConnection);

	/**
	 * Runs after the info task executes
	 * 
	 * @param dataConnection A valid connection to the database
	 */
	void afterInfo(Connection dataConnection);
}
