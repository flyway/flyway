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
package org.flywaydb.core.internal.dbsupport.sybase.ase;

import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

/**
 * Sybase schema (database) for flyway support
 *
 */
public class SybaseASESchema extends Schema<SybaseASEDbSupport> {

	public SybaseASESchema(JdbcTemplate jdbcTemplate, SybaseASEDbSupport dbSupport,
			String name) {
		super(jdbcTemplate, dbSupport, name);
	}

	@Override
	protected boolean doExists() throws SQLException {
		//There is no schema in Sybase. Always return true
		return true;
	}

	@Override
	protected boolean doEmpty() throws SQLException {
		//There is no schema in Sybase, check whether database is empty
		//Check for tables, views stored procs and triggers
		return jdbcTemplate.queryForInt("select count(*) from sysobjects ob where (ob.type='U' or ob.type = 'V' or ob.type = 'P' or ob.type = 'TR') and ob.name != 'sysquerymetrics'") == 0;
	}

	@Override
	protected void doCreate() throws SQLException {
		//There is no schema in Sybase. Do nothing for creation.
	}

	@Override
	protected void doDrop() throws SQLException {
		//There is no schema in Sybase, no schema can be dropped. Do nothing here.
	}

	/*
	 * This clean method is equivalent to cleaning the whole database.
	 * @see org.flywaydb.core.internal.dbsupport.Schema#doClean()
	 */
	@Override
	protected void doClean() throws SQLException {
		//Drop tables
		dropObjects("U");
		//Drop view
		dropObjects("V");
		//Drop stored procs
		dropObjects("P");
		//Drop triggers
		dropObjects("TR");
	}

	@Override
	protected Table[] doAllTables() throws SQLException {
		
		//Retrieving all table names
		List<String> tableNames = retrieveAllTableNames();
		
		Table[] result = new Table[tableNames.size()];
		
		for(int i = 0; i < tableNames.size(); i++) {
			String tableName = tableNames.get(i);
			result[i] = new SybaseASETable(jdbcTemplate, dbSupport, this, tableName);
		}
		
		return result;
	}

	@Override
	public Table getTable(String tableName) {
		return new SybaseASETable(jdbcTemplate, dbSupport, this, tableName);
	}
	
	/**
	 * Return all table names in the current database
	 * @return
	 * @throws SQLException
	 */
	private List<String> retrieveAllTableNames() throws SQLException {
		List<String> objNames = jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", "U");
		
		return objNames;
	}
	
	private void dropObjects(String sybaseObjType) throws SQLException {
		
		//Getting the table names
		List<String> objNames = jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", sybaseObjType);
		
		//for each table, drop it
		for (String name : objNames) {
			String sql = "";
			
			if ("U".equals(sybaseObjType)) {
				sql = "drop table ";
			} else if ("V".equals(sybaseObjType)) {
				sql = "drop view ";
			} else if ("P".equals(sybaseObjType)) {
				//dropping stored procedure
				sql = "drop procedure ";
			} else if ("TR".equals(sybaseObjType)) {
				sql = "drop trigger ";
			} else {
				throw new IllegalArgumentException("Unknown database object type " + sybaseObjType);
			}
			
			jdbcTemplate.execute(sql + name);
			
		}
	}

}
