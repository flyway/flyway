/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.sybase.asa;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.sybase.ase.SybaseASEDbSupport;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Sybase SQL Anywhere specific support
 *
 */
public class SybaseASADbSupport extends SybaseASEDbSupport {

	private static final Log LOG = LogFactory.getLog(SybaseASADbSupport.class);

	public SybaseASADbSupport(Connection connection) {
		super(connection, Types.INTEGER);
    }

	/*
	 * (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getSchema(java.lang.
	 * String)
	 */
	@Override
	public Schema getSchema(String name) {
		// Sybase does not support schema and changing user on the fly. Always
		// return a schema that does not exist
		Schema schema = new SybaseASASchema(jdbcTemplate, this, name) {
			@Override
			protected boolean doExists() throws SQLException {
				return false;
			}

		};

		try {
			String currentName = doGetCurrentSchemaName();
			if (currentName.equals(name)) {
				schema = new SybaseASASchema(jdbcTemplate, this, name);
			}
		} catch (SQLException e) {
			LOG.error("Unable to obtain current schema, return non-existing schema", e);
		}
		return schema;
	}


	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getDbName()
	 */
	@Override
	public String getDbName() {
		return "sybaseASA";
	}
}
