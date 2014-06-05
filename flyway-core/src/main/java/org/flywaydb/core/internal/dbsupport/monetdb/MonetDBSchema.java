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

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MonetDB implementation of Schema.
 */
public class MonetDBSchema extends Schema<MonetDBDbSupport> {
	/**
	 * Creates a new MySQL schema.
	 * 
	 * @param jdbcTemplate
	 *            The Jdbc Template for communicating with the DB.
	 * @param dbSupport
	 *            The database-specific support.
	 * @param name
	 *            The name of the schema.
	 */
	public MonetDBSchema(JdbcTemplate jdbcTemplate, MonetDBDbSupport dbSupport, String name) {
		super(jdbcTemplate, dbSupport, name);
	}

	@Override
	protected boolean doExists() throws SQLException {
		return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYS.SCHEMAS WHERE name=?", name) > 0;
	}

	@Override
	protected boolean doEmpty() throws SQLException {
		int objectCount = jdbcTemplate
				.queryForInt(
						"Select "
								+ "(Select count(*) from SYS.TABLES t, SYS.SCHEMAS s Where t.schema_id=s.id and s.name=?) + "
								+ "(Select count(*) from SYS.KEYS k, SYS.TABLES t, SYS.SCHEMAS s Where t.schema_id=s.id and t.id=k.table_id and s.name=?) + "
								+ "(Select count(*) from SYS.SEQUENCES sq, SYS.SCHEMAS s Where sq.schema_id=s.id and s.name=?) + "
								+ "(Select count(*) from SYS.FUNCTIONS f, SYS.SCHEMAS s Where f.schema_id=s.id and s.name=?)",
						name, name, name, name);
		return objectCount == 0;
	}

	@Override
	protected void doCreate() throws SQLException {
		boolean exists = jdbcTemplate.queryForInt("select count(*) from sys.schemas where name = ?", name) > 0;
		
		if(!exists) {
			String savedRole = jdbcTemplate.queryForString("select CURRENT_ROLE");
			String user = jdbcTemplate.queryForString("select CURRENT_USER");
			
			jdbcTemplate.execute("set role \"sysadmin\"");
			jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name) + " AUTHORIZATION " + user);
			jdbcTemplate.execute("set role \"" + savedRole + "\"");
		}
	}

	@Override
	protected void doDrop() throws SQLException {
		jdbcTemplate.execute("set role flyway");
		jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " cascade");
	}

	@Override
	protected void doClean() throws SQLException {
		for (String statement : cleanFks()) {
			jdbcTemplate.execute(statement);
		}

		for (String statement : cleanFunctions()) {
			jdbcTemplate.execute(statement);
		}

		for (String statement : cleanViews()) {
			jdbcTemplate.execute(statement);
		}

		for (Table table : allTables()) {
			table.drop();
		}
		
		for (String statement : cleanSequences()) {
			jdbcTemplate.execute(statement);
		}
	}

	/**
	 * Generate the statements to clean the foreign keys and indices in this
	 * schema.
	 * 
	 * @return The list of statements.
	 * @throws SQLException
	 *             when the clean statements could not be generated.
	 */
	private List<String> cleanFks() throws SQLException {
		List<Map<String, String>> fkNames = jdbcTemplate
				.queryForList(
						"SELECT k.name as name FROM SYS.KEYS k, SYS.SCHEMAS s, SYS.TABLES t WHERE k.type = 2 and k.table_id = t.id and t.schema_id = s.id and s.name=?",
						name);

		List<String> statements = new ArrayList<String>();
		for (Map<String, String> row : fkNames) {
			statements.add("DROP index " + dbSupport.quote(name, row.get("name")) + "");
		}
		return statements;
	}

	/**
	 * Generate the statements to clean all sequences in this schema.
	 * 
	 * @return The list of statements.
	 * @throws SQLException
	 *             when the clean statements could not be generated.
	 */
	private List<String> cleanSequences() throws SQLException {
		List<String> sequences = jdbcTemplate.queryForStringList(
				"Select sq.name from SYS.SEQUENCES sq, SYS.SCHEMAS s Where sq.schema_id=s.id and s.name=?",
				name);
		List<String> statements = new ArrayList<String>();
		for (String sqName : sequences) {
			statements.add("DROP sequence " + dbSupport.quote(name, sqName));
		}
		return statements;
		
	}

	/**
	 * Generate the statements to clean the routines in this schema.
	 * 
	 * @return The list of statements.
	 * @throws SQLException
	 *             when the clean statements could not be generated.
	 */
	private List<String> cleanFunctions() throws SQLException {
		List<Map<String, String>> routineNames = jdbcTemplate
				.queryForList(
						"SELECT f.name, f.type as type FROM SYS.FUNCTIONS f, SYS.SCHEMAS s WHERE s.name=? and s.id=f.schema_id",
						name);

		List<String> statements = new ArrayList<String>();
		for (Map<String, String> row : routineNames) {
			String funcName = row.get("name");
			String type = row.get("type");
			if ("1".equals(type)) {
				statements.add("DROP function " + dbSupport.quote(name, funcName) + " CASCADE");
			} else if ("2".equals(type)) {
				statements.add("DROP procedure " + dbSupport.quote(name, funcName) + " CASCADE");
			}
		}
		return statements;
	}

	/**
	 * Generate the statements to clean the views in this schema.
	 * 
	 * @return The list of statements.
	 * @throws SQLException
	 *             when the clean statements could not be generated.
	 */
	private List<String> cleanViews() throws SQLException {
		List<String> viewNames = jdbcTemplate
				.queryForStringList(
						"SELECT t.name FROM SYS.TABLES t, SYS.SCHEMAS s WHERE s.name=? and s.id=t.schema_id and t.type=1",
						name);

		List<String> statements = new ArrayList<String>();
		for (String viewName : viewNames) {
			statements.add("DROP VIEW " + dbSupport.quote(name, viewName) + " CASCADE");
		}
		return statements;
	}

	@Override
	protected Table[] doAllTables() throws SQLException {
		List<String> tableNames = jdbcTemplate
				.queryForStringList(
						"SELECT t.name FROM SYS.TABLES t, SYS.SCHEMAS s WHERE s.name=? and s.id=t.schema_id and t.type=0",
						name);

		Table[] tables = new Table[tableNames.size()];
		for (int i = 0; i < tableNames.size(); i++) {
			tables[i] = new MonetDBTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
		}
		return tables;
	}

	@Override
	public Table getTable(String tableName) {
		return new MonetDBTable(jdbcTemplate, dbSupport, this, tableName);
	}
}
