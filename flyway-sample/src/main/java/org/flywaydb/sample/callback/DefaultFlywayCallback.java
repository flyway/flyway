/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.sample.callback;

import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.MigrationInfo;

import java.sql.Connection;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

/**
 * Default FlywayCallback implementation.  If you don't want to have to 
 * implement all of the FlywayCallback methods you can use this as your
 * base class and override the relavant callbacks you are interested in.
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
	public void beforeUndo(Connection connection) {
		LOG.info("beforeUndo");
	}

	@Override
	public void beforeEachUndo(Connection connection, MigrationInfo info) {
		LOG.info("beforeEachUndo");
	}

	@Override
	public void afterEachUndo(Connection connection, MigrationInfo info) {
		LOG.info("afterEachUndo");
	}

	@Override
	public void afterUndo(Connection connection) {
		LOG.info("afterUndo");
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
