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
package org.flywaydb.sample.callback;

import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.MigrationInfo;

import java.sql.Connection;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Default FlywayCallback implementation.  If you don't want to have to 
 * implement all of the FlywayCallback methods you can use this as your
 * base class and override the relavant callbacks you are interested in.
 * 
 * @author Dan Bunker
 *
 */
public class DefaultFlywayCallback implements FlywayCallback {
    private static final Log LOG = LogFactory.getLog(DefaultFlywayCallback.class);

    @Override
	public void beforeClean(Connection dataConnection) {
    	LOG.info("beforeClean");
	}

	@Override
	public void afterClean(Connection dataConnection) {
		LOG.info("afterClean");
	}

	@Override
	public void beforeMigrate(Connection dataConnection) {
		LOG.info("beforeMigrate");
	}

	@Override
	public void afterMigrate(Connection dataConnection) {
		LOG.info("afterMigrate");
	}

	@Override
	public void beforeEachMigrate(Connection dataConnection, MigrationInfo info) {
		LOG.info("beforeEachMigrate");
	}

	@Override
	public void afterEachMigrate(Connection dataConnection, MigrationInfo info) {
		LOG.info("afterEachMigrate");
	}

	@Override
	public void beforeValidate(Connection dataConnection) {
		LOG.info("beforeValidate");
	}

	@Override
	public void afterValidate(Connection dataConnection) {
		LOG.info("afterValidate");
	}

	@Override
	public void beforeBaseline(Connection connection) {
		LOG.info("beforeBaseline");
	}

	@Override
	public void afterBaseline(Connection connection) {
		LOG.info("afterBaseline");
	}

	@Override
	public void beforeRepair(Connection dataConnection) {
		LOG.info("beforeRepair");
	}

	@Override
	public void afterRepair(Connection dataConnection) {
		LOG.info("afterRepair");
	}

	@Override
	public void beforeInfo(Connection dataConnection) {
		LOG.info("beforeInfo");
	}

	@Override
	public void afterInfo(Connection dataConnection) {
		LOG.info("afterInfo");
	}
}
