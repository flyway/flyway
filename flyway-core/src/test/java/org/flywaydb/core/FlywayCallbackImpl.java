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
package org.flywaydb.core;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;

import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.MigrationInfo;

/**
 * Sample FlywayCallback implementation to test that the lifecycle
 * notifications are getting called correctly
 *
 * @author Dan Bunker
 */
public class FlywayCallbackImpl implements FlywayCallback, ConfigurationAware {
    private FlywayConfiguration flywayConfiguration;
	private int beforeClean;
	private int afterClean;
	private int beforeMigrate;
	private int afterMigrate;
	private int beforeEachMigrate;
	private int afterEachMigrate;
	private int beforeUndo;
	private int afterUndo;
	private int beforeEachUndo;
	private int afterEachUndo;
	private int beforeValidate;
	private int afterValidate;
	private int beforeBaseline;
	private int afterBaseline;
	private int beforeRepair;
	private int afterRepair;
	private int beforeInfo;
	private int afterInfo;

	@Override
	public void beforeClean(Connection dataConnection) {
		beforeClean++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterClean(Connection dataConnection) {
		afterClean++;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeMigrate(Connection dataConnection) {
		beforeMigrate++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterMigrate(Connection dataConnection) {
		afterMigrate++;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeEachMigrate(Connection dataConnection, MigrationInfo info) {
		beforeEachMigrate++;
        assertNotNull(dataConnection);
        assertNotNull(info);
	}

	@Override
	public void afterEachMigrate(Connection dataConnection, MigrationInfo info) {
		afterEachMigrate++;
        assertNotNull(dataConnection);
        assertNotNull(info);
	}

	@Override
	public void beforeUndo(Connection dataConnection) {
		beforeUndo++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterUndo(Connection dataConnection) {
		afterUndo++;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeEachUndo(Connection dataConnection, MigrationInfo info) {
		beforeEachUndo++;
        assertNotNull(dataConnection);
        assertNotNull(info);
	}

	@Override
	public void afterEachUndo(Connection dataConnection, MigrationInfo info) {
		afterEachUndo++;
        assertNotNull(dataConnection);
        assertNotNull(info);
	}

	@Override
	public void beforeValidate(Connection dataConnection) {
		beforeValidate++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterValidate(Connection dataConnection) {
		afterValidate++;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeBaseline(Connection dataConnection) {
		beforeBaseline++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterBaseline(Connection dataConnection) {
		afterBaseline++;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeRepair(Connection dataConnection) {
		beforeRepair++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterRepair(Connection dataConnection) {
		afterRepair++;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeInfo(Connection dataConnection) {
		beforeInfo++;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterInfo(Connection dataConnection) {
		afterInfo++;
        assertNotNull(dataConnection);
	}

	@Override
	public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
	}

	public int getBeforeClean() {
		return beforeClean;
	}

	public int getAfterClean() {
		return afterClean;
	}

	public int getBeforeMigrate() {
		return beforeMigrate;
	}

	public int getAfterMigrate() {
		return afterMigrate;
	}

	public int getBeforeEachMigrate() {
		return beforeEachMigrate;
	}

	public int getAfterEachMigrate() {
		return afterEachMigrate;
	}

	public int getBeforeUndo() {
		return beforeUndo;
	}

	public int getAfterUndo() {
		return afterUndo;
	}

	public int getBeforeEachUndo() {
		return beforeEachUndo;
	}

	public int getAfterEachUndo() {
		return afterEachUndo;
	}

	public int getBeforeValidate() {
		return beforeValidate;
	}

	public int getAfterValidate() {
		return afterValidate;
	}

	public int getBeforeBaseline() {
		return beforeBaseline;
	}

	public int getAfterBaseline() {
		return afterBaseline;
	}

	public int getBeforeRepair() {
		return beforeRepair;
	}

	public int getAfterRepair() {
		return afterRepair;
	}

	public int getBeforeInfo() {
		return beforeInfo;
	}

	public int getAfterInfo() {
		return afterInfo;
	}

	public void assertFlywayConfigurationSet() {
		assertNotNull("Configuration must have been set", flywayConfiguration);
	}
}
