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
import java.sql.Statement;
import java.util.Random;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * This class is responsible for createing and relasing locks during migration process. In MonetDB it's not possible to lock metadata table using select ... for update. 
 * In distributed environments user must declare this class as callback managing locks.
 * 
 * @author Radosław Ostrzycki
 * 
 */
public class MonetDBConcurrentLockManager implements FlywayCallback {
	private static final Log LOG = LogFactory.getLog(MonetDBConcurrentLockManager.class);

	private final static String LOCK_TABLE = "flyway_lock_table_4_monetdb";
	
	@Override
	public void beforeClean(Connection dataConnection) {
	}

	@Override
	public void afterClean(Connection dataConnection) {
	}

	/**
	 * This method tries to create "lock" table - if it fails, it tries again and again...
	 */
	@Override
	public void beforeMigrate(Connection dataConnection) {
		boolean hasLock = false;
		while (!hasLock) {
			Statement stmt = null;
			try {
				try {
					Thread.sleep(100 + new Random().nextInt(500));
				} catch (InterruptedException e) {
				}
				
				stmt = dataConnection.createStatement();
				stmt.execute("create table " + LOCK_TABLE + " (i int)");
				stmt.close();
				dataConnection.commit();
				hasLock = true;
				LOG.debug("Lock acquired.");
			} catch (SQLException e) {
				try {
					stmt.close();
					dataConnection.rollback();
				} catch (SQLException e2) {
				}
				try {
					LOG.debug("Lock failed - waiting...");
					Thread.sleep(500 + new Random().nextInt(1500));
				} catch (InterruptedException e1) {
				}
			}
		}
		
	}

	/**
	 * Releaseing lock (dropping lock table).
	 */
	@Override
	public void afterMigrate(Connection dataConnection) {
		LOG.info("afterMigrate: release locks.");
		Statement stmt = null;
		try {
			stmt = dataConnection.createStatement();
			stmt.execute("drop table " + LOCK_TABLE);
			stmt.close();
			dataConnection.commit();
		} catch (SQLException e) {
			try {
				stmt.close();
			} catch (SQLException e2) {
			}
		}
		
	}

	@Override
	public void beforeEachMigrate(Connection dataConnection, MigrationInfo info) {
	}

	@Override
	public void afterEachMigrate(Connection dataConnection, MigrationInfo info) {
	}

	@Override
	public void beforeValidate(Connection dataConnection) {
	}

	@Override
	public void afterValidate(Connection dataConnection) {
	}

	@Override
	public void beforeInit(Connection dataConnection) {
	}

	@Override
	public void afterInit(Connection dataConnection) {
	}

	@Override
	public void beforeRepair(Connection dataConnection) {
	}

	@Override
	public void afterRepair(Connection dataConnection) {
	}

	@Override
	public void beforeInfo(Connection dataConnection) {
	}

	@Override
	public void afterInfo(Connection dataConnection) {
	}
}