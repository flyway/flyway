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
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

import com.mongodb.MongoClient;

/**
 * Convenience base no-op implementation of MongoFlywayCallback. Extend this class if you want to
 * implement just a few callback methods without having to provide no-op methods yourself.
 *
 * <p>This implementation also provides direct access to the {@link FlywayConfiguration} as field.</p>
 */
public class BaseMongoFlywayCallback extends MongoFlywayCallback {

	public BaseMongoFlywayCallback(FlywayConfiguration flywayConfiguration) {
		super(flywayConfiguration);
	}
	
	@Override
	public void beforeClean(MongoClient connection) {
	}

	@Override
	public void afterClean(MongoClient connection) {
	}

	@Override
	public void beforeMigrate(MongoClient connection) {
	}

	@Override
	public void afterMigrate(MongoClient connection) {
	}

	@Override
	public void beforeEachMigrate(MongoClient connection, MigrationInfo info) {
	}

	@Override
	public void afterEachMigrate(MongoClient connection, MigrationInfo info) {
	}

	@Override
	public void beforeValidate(MongoClient connection) {
	}

	@Override
	public void afterValidate(MongoClient connection) {
	}

	@Override
	public void beforeBaseline(MongoClient connection) {
	}

	@Override
	public void afterBaseline(MongoClient connection) {
	}

	@Override
	public void beforeRepair(MongoClient connection) {
	}

	@Override
	public void afterRepair(MongoClient connection) {
	}

	@Override
	public void beforeInfo(MongoClient connection) {
	}

	@Override
	public void afterInfo(MongoClient connection) {
	}
}
