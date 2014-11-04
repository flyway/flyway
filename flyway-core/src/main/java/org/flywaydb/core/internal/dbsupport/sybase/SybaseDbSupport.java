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
package org.flywaydb.core.internal.dbsupport.sybase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Sybase specific support
 *
 */
public class SybaseDbSupport extends DbSupport {

	private static final Log LOG = LogFactory.getLog(SybaseDbSupport.class);
	
	public SybaseDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getSchema(java.lang.String)
	 */
	@Override
	public Schema getSchema(String name) {
		return new SybaseSchema(jdbcTemplate, this, name);
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#createSqlStatementBuilder()
	 */
	@Override
	public SqlStatementBuilder createSqlStatementBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#getDbName()
	 */
	@Override
	public String getDbName() {
		return "sybase";
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#doGetCurrentSchema()
	 */
	@Override
	protected String doGetCurrentSchema() throws SQLException {
		return jdbcTemplate.queryForString("select USER_NAME()");
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#doSetCurrentSchema(org.flywaydb.core.internal.dbsupport.Schema)
	 */
	@Override
	protected void doSetCurrentSchema(Schema schema) throws SQLException {
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
		return "'" + StringUtils.replaceAll(identifier, "'", "\'") + "'";
	}

	/* (non-Javadoc)
	 * @see org.flywaydb.core.internal.dbsupport.DbSupport#catalogIsSchema()
	 */
	@Override
	public boolean catalogIsSchema() {
		return false;
	}

}
