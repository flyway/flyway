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
package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class Neo4JDbSupport extends DbSupport {
	 
	private static final Log LOG = LogFactory.getLog(Neo4JDbSupport.class);
	
	public Neo4JDbSupport(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}
	
	public Neo4JDbSupport(Connection connection) {
		 super(new JdbcTemplate(connection, Types.NULL));
	}

	@Override
	public Schema<DbSupport> getSchema(String name) {
		return new Neo4JSchema(jdbcTemplate, this, name);
	}

	@Override
	public SqlStatementBuilder createSqlStatementBuilder() {
		return new Neo4JSqlStatementBuilder();
	}

	@Override
	public String getDbName() {
		return "Neo4J";
	}

	@Override
	protected String doGetCurrentSchemaName() throws SQLException {
		return "No Database Schema";
	}

	@Override
	protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
		LOG.info("Neo4J does not support schema. Default schema NOT changed to " + schema);
	}

	@Override
	public String getCurrentUserFunction() {
		return "user";
	}

	@Override
	public boolean supportsDdlTransactions() {
		return false;
	}

	@Override
	public String getBooleanTrue() {
		return "true";
	}

	@Override
	public String getBooleanFalse() {
		return "false";
	}

	@Override
	protected String doQuote(String identifier) {
		  return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
	}

	@Override
	public boolean catalogIsSchema() {
		return false;
	}
	
	

}
