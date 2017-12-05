/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.ConfigurationAware;

import java.sql.Connection;

/**
 * This is the main callback interface that should be implemented to get access to flyway lifecycle notifications.
 * Simply add code to the callback method you are interested in having. A convenience implementation with all methods
 * doing nothing is provided with {@link BaseFlywayCallback}. To ensure backward compatibility, you are encouraged
 * to subclass that class instead of implementing this interface directly.
 *
 * <p>If a callback also implements the {@link ConfigurationAware} interface,
 * a {@link org.flywaydb.core.api.configuration.FlywayConfiguration} object will automatically be injected before
 * calling any methods, giving the callback access to the core flyway configuration. {@link BaseFlywayCallback}
 * already implements {@link ConfigurationAware}</p>
 *
 * <p>Each callback method will run within its own transaction.</p>
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
	 * Runs before the undo task executes.
	 *
	 * @param connection A valid connection to the database.
	 */
	void beforeUndo(Connection connection);

	/**
	 * Runs before each migration script is undone.
	 *
	 * @param connection A valid connection to the database.
	 * @param info The current MigrationInfo for the migration to be undone.
	 */
	void beforeEachUndo(Connection connection, MigrationInfo info);

	/**
	 * Runs after each migration script is undone.
	 *
	 * @param connection A valid connection to the database.
	 * @param info The current MigrationInfo for the migration just undone.
	 */
	void afterEachUndo(Connection connection, MigrationInfo info);

	/**
	 * Runs after the undo task executes.
	 *
	 * @param connection A valid connection to the database.
	 */
	void afterUndo(Connection connection);

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
