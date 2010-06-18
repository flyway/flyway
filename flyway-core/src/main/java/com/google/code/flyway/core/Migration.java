/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * A migration of a single version of the schema.
 * 
 * @author Axel Fontaine
 */
public abstract class Migration {
	/**
	 * The target schema version of this migration.
	 */
	protected SchemaVersion schemaVersion = SchemaVersion.EMPTY;

	/**
	 * The state of this migration.
	 */
	protected MigrationState migrationState = MigrationState.UNKNOWN;

	/**
	 * The time (in ms) it took to execute.
	 */
	protected int executionTime = -1;

	/**
	 * The script name for the migration history.
	 */
	protected String scriptName;

	/**
	 * @return The schema version after the migration is complete.
	 */
	public SchemaVersion getVersion() {
		return schemaVersion;
	}

	/**
	 * @return The state of this migration.
	 */
	public MigrationState getState() {
		return migrationState;
	}

	/**
	 * @return The time (in ms) it took to execute.
	 */
	public long getExecutionTime() {
		return executionTime;
	}

	/**
	 * @return The script name for the migration history.
	 */
	public String getScriptName() {
		return scriptName;
	}

	/**
	 * Asserts that this migration has not failed.
	 * 
	 * @throws IlllegalStateException
	 *             Thrown when this migration has failed.
	 */
	public void assertNotFailed() {
		if (MigrationState.FAILED == migrationState) {
			throw new IllegalStateException("Migration to version " + schemaVersion
					+ " failed! Please restore backups and roll back database and code!");
		}
	}

	/**
	 * Performs the migration. The migration state and the execution time are
	 * updated accordingly.
	 * 
	 * @param jdbcTemplate
	 *            To execute the migration statements.
	 */
	public abstract void migrate(SimpleJdbcTemplate jdbcTemplate);
}
