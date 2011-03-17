/**
 * Copyright (C) 2010-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.googlecode.flyway.core.dbsupport.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;

/**
 * DB2 Support.
 */
public class Db2DbSupport implements DbSupport
{
	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(Db2DbSupport.class);

	/**
	 * The jdbcTemplate to use.
	 */
	private final JdbcTemplate jdbcTemplate;

	/**
	 * @param jdbcTemplate
	 *            to use
	 */
	public Db2DbSupport(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#createSqlScript(java.lang.String,
	 *      com.googlecode.flyway.core.migration.sql.PlaceholderReplacer)
	 */
	@Override
	public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer)
	{
		return new Db2SqlScript(sqlScriptSource, placeholderReplacer);
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#createCleanScript()
	 */
	@Override
	public SqlScript createCleanScript()
	{
		// TODO PROCEDURES and FUNCTIONS
		final List<String> allDropStatements = new ArrayList<String>();

		String currentSchema = getCurrentSchema();

		// views
		String dropViewsGenQuery = "select rtrim(VIEWNAME) from SYSCAT.VIEWS where VIEWSCHEMA = '" + currentSchema
				+ "'";
		List<String> dropViewsStatements = buildDropStatements("drop view", currentSchema, dropViewsGenQuery);
		allDropStatements.addAll(dropViewsStatements);

		// tables
		String dropTablesGenQuery = "select rtrim(TABNAME) from SYSCAT.TABLES where TABSCHEMA = '" + currentSchema
				+ "'";
		List<String> dropTableStatements = buildDropStatements("drop table", currentSchema, dropTablesGenQuery);
		allDropStatements.addAll(dropTableStatements);

		// sequences
		String dropSeqGenQuery = "select rtrim(SEQNAME) from SYSCAT.SEQUENCES where SEQSCHEMA = '" + currentSchema
				+ "'";
		List<String> dropSeqStatements = buildDropStatements("drop sequence", currentSchema, dropSeqGenQuery);
		allDropStatements.addAll(dropSeqStatements);

		// indices in DB2 are deleted, if the corresponding table is dropped

		List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
		int count = 0;
		for (String dropStatement : allDropStatements) {
			count++;
			sqlStatements.add(new SqlStatement(count, dropStatement));
		}

		return new SqlScript(sqlStatements);
	}

	/**
	 * Builds the drop statements for database objects.
	 * 
	 * @param query
	 *            the query to get all present database objects
	 * @param dropPrefix
	 *            the drop command for the database object (e.g. 'drop table').
	 * @return
	 */
	private List<String> buildDropStatements(final String dropPrefix, final String schema, final String query)
	{
		List<String> dropStatements = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		List<String> dbObjects = jdbcTemplate.queryForList(query, String.class);
		for (String dbObject : dbObjects) {
			// DB2 needs double quotes
			dropStatements.add(dropPrefix + " \"" + schema + "\".\"" + dbObject + "\"");
		}
		return dropStatements;
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#getScriptLocation()
	 */
	@Override
	public String getScriptLocation()
	{
		return "com/googlecode/flyway/core/dbsupport/db2/";
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#isSchemaEmpty()
	 */
	@Override
	public boolean isSchemaEmpty()
	{
		int objectCount = jdbcTemplate
				.queryForInt("select count(*) from syscat.tables where tabschema = CURRENT_SCHEMA");
		objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.views where viewschema = CURRENT_SCHEMA");
		objectCount += jdbcTemplate
				.queryForInt("select count(*) from syscat.sequences where seqschema = CURRENT_SCHEMA");
		objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.indexes where indschema = CURRENT_SCHEMA");
		return objectCount == 0;
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#tableExists(java.lang.String)
	 */
	@Override
	public boolean tableExists(final String table)
	{
		return (Boolean) jdbcTemplate.execute(new ConnectionCallback()
		{
			@Override
			public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException
			{
				ResultSet resultSet = connection.getMetaData().getTables(null, getCurrentSchema(), table.toUpperCase(),
						null);
				return resultSet.next();
			}
		});
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#columnExists(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean columnExists(final String table, final String column)
	{
		return (Boolean) jdbcTemplate.execute(new ConnectionCallback()
		{
			@Override
			public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException
			{
				ResultSet resultSet = connection.getMetaData().getColumns(null, getCurrentSchema(),
						table.toUpperCase(), column.toUpperCase());
				return resultSet.next();
			}
		});
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#getCurrentSchema()
	 */
	@Override
	public String getCurrentSchema()
	{
		return ((String) jdbcTemplate.queryForObject("select current_schema from sysibm.sysdummy1", String.class))
				.trim();
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#getCurrentUserFunction()
	 */
	@Override
	public String getCurrentUserFunction()
	{
		return "CURRENT_USER";
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#supportsDdlTransactions()
	 */
	@Override
	public boolean supportsDdlTransactions()
	{
		return true;
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#lockTable(java.lang.String)
	 */
	@Override
	public void lockTable(String table)
	{
		jdbcTemplate.execute("lock table " + table + " in exclusive mode");
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#getBooleanTrue()
	 */
	@Override
	public String getBooleanTrue()
	{
		return "1";
	}

	/**
	 * @see com.googlecode.flyway.core.dbsupport.DbSupport#getBooleanFalse()
	 */
	@Override
	public String getBooleanFalse()
	{
		return "0";
	}

}
