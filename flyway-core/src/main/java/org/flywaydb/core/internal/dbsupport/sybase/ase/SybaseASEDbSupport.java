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
package org.flywaydb.core.internal.dbsupport.sybase.ase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Sybase specific support
 *
 */
public class SybaseASEDbSupport extends DbSupport {

	private static final Log LOG = LogFactory.getLog(SybaseASEDbSupport.class);
	
	public SybaseASEDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getSchema(java.lang.String)
	 */
	@Override
	public Schema getSchema(String name) {
		//Sybase does not support schema and changing user on the fly. Always return a schema that does not exist
		Schema schema = new SybaseASESchema(jdbcTemplate, this, name) {
			@Override
			protected boolean doExists() throws SQLException {
				return false;
			}
			
		};
		
		try {
			String currentName = doGetCurrentSchemaName();
			if (currentName.equals(name)) {
				schema = new SybaseASESchema(jdbcTemplate, this, name);
			}
		} catch (SQLException e) {
			LOG.error("Unable to obtain current schema, return non-existing schema", e);
		}
		return schema;
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#createSqlStatementBuilder()
	 */
	@Override
	public SqlStatementBuilder createSqlStatementBuilder() {
		return new SybaseASESqlStatementBuilder();
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getDbName()
	 */
	@Override
	public String getDbName() {
		return "sybaseASE";
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#doGetCurrentSchemaName()
	 */
	@Override
	protected String doGetCurrentSchemaName() throws SQLException {
		return jdbcTemplate.queryForString("select USER_NAME()");
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#doChangeCurrentSchemaTo(org.flywaydb.core.internal.dbsupport.Schema)
	 */
	@Override
	protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
		LOG.info("Sybase does not support setting the schema for the current session. Default schema NOT changed to " + schema);
        // Not currently supported.
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getCurrentUserFunction()
	 */
	@Override
	public String getCurrentUserFunction() {
		return "user_name()";
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#supportsDdlTransactions()
	 */
	@Override
	public boolean supportsDdlTransactions() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getBooleanTrue()
	 */
	@Override
	public String getBooleanTrue() {
		return "1";
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getBooleanFalse()
	 */
	@Override
	public String getBooleanFalse() {
		return "0";
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#doQuote(java.lang.String)
	 */
	@Override
	protected String doQuote(String identifier) {
		//Sybase doesn't quote identifiers, skip quotting
		return identifier;
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#catalogIsSchema()
	 */
	@Override
	public boolean catalogIsSchema() {
		return false;
	}

}
