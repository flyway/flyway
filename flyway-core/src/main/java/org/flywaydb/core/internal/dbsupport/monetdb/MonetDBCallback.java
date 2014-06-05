/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.monetdb;

import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.MigrationInfo;

import java.sql.Connection;
import java.sql.SQLException;

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
public class MonetDBCallback implements FlywayCallback {
    private static final Log LOG = LogFactory.getLog(MonetDBCallback.class);

    @Override
	public void beforeClean(Connection dataConnection) {
    	LOG.debug("beforeClean");
	}

	@Override
	public void afterClean(Connection dataConnection) {
		LOG.debug("afterClean");
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
		LOG.debug("beforeEachMigrate");
	}

	@Override
	public void afterEachMigrate(Connection dataConnection, MigrationInfo info) {
		LOG.debug("afterEachMigrate");
	}

	@Override
	public void beforeValidate(Connection dataConnection) {
		LOG.debug("beforeValidate");
	}

	@Override
	public void afterValidate(Connection dataConnection) {
		LOG.debug("afterValidate");
	}

	@Override
	public void beforeInit(Connection dataConnection) {
		LOG.debug("beforeInit");
	}

	@Override
	public void afterInit(Connection dataConnection) {
		LOG.debug("afterInit");
	}

	@Override
	public void beforeRepair(Connection dataConnection) {
		LOG.debug("beforeRepair");
	}

	@Override
	public void afterRepair(Connection dataConnection) {
		LOG.debug("afterRepair");
	}

	@Override
	public void beforeInfo(Connection dataConnection) {
		LOG.debug("beforeInfo");
	}

	@Override
	public void afterInfo(Connection dataConnection) {
		LOG.debug("afterInfo");
	}
}