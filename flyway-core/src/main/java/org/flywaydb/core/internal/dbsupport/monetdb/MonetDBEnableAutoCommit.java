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
public class MonetDBEnableAutoCommit implements FlywayCallback {
    private static final Log LOG = LogFactory.getLog(MonetDBEnableAutoCommit.class);

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
		LOG.info("beforeMigrate: setting AutoCommit to true");
	}

	@Override
	public void afterMigrate(Connection dataConnection) {
		LOG.info("afterMigrate: setting AutoCommit to false");

	}

	@Override
	public void beforeEachMigrate(Connection dataConnection, MigrationInfo info) {
		LOG.debug("beforeEachMigrate");
//		try {
//			//dataConnection.commit();
//			//dataConnection.setAutoCommit(true);
//		} catch (SQLException e) {
//			LOG.error(e.getMessage(), e);
//		}
		
	}

	@Override
	public void afterEachMigrate(Connection dataConnection, MigrationInfo info) {
		LOG.debug("afterEachMigrate");
		try {
			dataConnection.commit();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}

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