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
	private boolean beforeClean = false;
	private boolean afterClean = false;
	private boolean beforeMigrate = false;
	private boolean afterMigrate = false;
	private boolean beforeEachMigrate = false;
	private boolean afterEachMigrate = false;
	private boolean beforeValidate = false;
	private boolean afterValidate = false;
	private boolean beforeBaseline = false;
	private boolean afterBaseline = false;
	private boolean beforeRepair = false;
	private boolean afterRepair = false;
	private boolean beforeInfo = false;
	private boolean afterInfo = false;

	@Override
	public void beforeClean(Connection dataConnection) {
		beforeClean = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterClean(Connection dataConnection) {
		afterClean = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeMigrate(Connection dataConnection) {
		beforeMigrate = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterMigrate(Connection dataConnection) {
		afterMigrate = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeEachMigrate(Connection dataConnection, MigrationInfo info) {
		beforeEachMigrate = true;
        assertNotNull(dataConnection);
        assertNotNull(info);
	}

	@Override
	public void afterEachMigrate(Connection dataConnection, MigrationInfo info) {
		afterEachMigrate = true;
        assertNotNull(dataConnection);
        assertNotNull(info);
	}

	@Override
	public void beforeValidate(Connection dataConnection) {
		beforeValidate = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterValidate(Connection dataConnection) {
		afterValidate = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeBaseline(Connection dataConnection) {
		beforeBaseline = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterBaseline(Connection dataConnection) {
		afterBaseline = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeRepair(Connection dataConnection) {
		beforeRepair = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterRepair(Connection dataConnection) {
		afterRepair = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void beforeInfo(Connection dataConnection) {
		beforeInfo = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void afterInfo(Connection dataConnection) {
		afterInfo = true;
        assertNotNull(dataConnection);
	}

	@Override
	public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
	}

	public boolean isBeforeClean() {
		return beforeClean;
	}

	public boolean isAfterClean() {
		return afterClean;
	}

	public boolean isBeforeMigrate() {
		return beforeMigrate;
	}

	public boolean isAfterMigrate() {
		return afterMigrate;
	}

	public boolean isBeforeEachMigrate() {
		return beforeEachMigrate;
	}

	public boolean isAfterEachMigrate() {
		return afterEachMigrate;
	}

	public boolean isBeforeValidate() {
		return beforeValidate;
	}

	public boolean isAfterValidate() {
		return afterValidate;
	}

	public boolean isBeforeBaseline() {
		return beforeBaseline;
	}

	public boolean isAfterBaseline() {
		return afterBaseline;
	}

	public boolean isBeforeRepair() {
		return beforeRepair;
	}

	public boolean isAfterRepair() {
		return afterRepair;
	}

	public boolean isBeforeInfo() {
		return beforeInfo;
	}

	public boolean isAfterInfo() {
		return afterInfo;
	}

	public void assertFlywayConfigurationSet() {
		assertNotNull("Configuration must have been set", flywayConfiguration);
	}
}
