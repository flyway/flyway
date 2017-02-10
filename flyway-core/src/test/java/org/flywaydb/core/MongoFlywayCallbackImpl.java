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

import com.mongodb.MongoClient;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;
import org.flywaydb.core.internal.callback.MongoScriptFlywayCallback;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.scanner.Scanner;

import static org.junit.Assert.assertNotNull;

/**
 * Sample FlywayCallback implementation to test that the lifecycle
 * notifications are getting called correctly
 *
 * @author Dan Bunker
 */
public class MongoFlywayCallbackImpl extends MongoFlywayCallback {

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

	/**
	 * Creates a new instance.
	 *
	 * @param configuration The Mongo configuration object
	 */
	public MongoFlywayCallbackImpl(MongoFlywayConfiguration configuration) {
		super(configuration);
	}

	@Override
	public void beforeClean(MongoClient client) {
		beforeClean = true;
        assertNotNull(client);
	}

	@Override
	public void afterClean(MongoClient client) {
		afterClean = true;
        assertNotNull(client);
	}

	@Override
	public void beforeMigrate(MongoClient client) {
		beforeMigrate = true;
        assertNotNull(client);
	}

	@Override
	public void afterMigrate(MongoClient client) {
		afterMigrate = true;
        assertNotNull(client);
	}

	@Override
	public void beforeEachMigrate(MongoClient client, MigrationInfo info) {
		beforeEachMigrate = true;
        assertNotNull(client);
        assertNotNull(info);
	}

	@Override
	public void afterEachMigrate(MongoClient client, MigrationInfo info) {
		afterEachMigrate = true;
        assertNotNull(client);
        assertNotNull(info);
	}

	@Override
	public void beforeValidate(MongoClient client) {
		beforeValidate = true;
        assertNotNull(client);
	}

	@Override
	public void afterValidate(MongoClient client) {
		afterValidate = true;
        assertNotNull(client);
	}

	@Override
	public void beforeBaseline(MongoClient client) {
		beforeBaseline = true;
        assertNotNull(client);
	}

	@Override
	public void afterBaseline(MongoClient client) {
		afterBaseline = true;
        assertNotNull(client);
	}

	@Override
	public void beforeRepair(MongoClient client) {
		beforeRepair = true;
        assertNotNull(client);
	}

	@Override
	public void afterRepair(MongoClient client) {
		afterRepair = true;
        assertNotNull(client);
	}

	@Override
	public void beforeInfo(MongoClient client) {
		beforeInfo = true;
        assertNotNull(client);
	}

	@Override
	public void afterInfo(MongoClient client) {
		afterInfo = true;
        assertNotNull(client);
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

}
