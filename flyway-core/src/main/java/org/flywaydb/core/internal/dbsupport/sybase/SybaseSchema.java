package org.flywaydb.core.internal.dbsupport.sybase;

import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

/**
 * Sybase schema (database) for flyway support
 *
 */
public class SybaseSchema extends Schema<SybaseDbSupport> {

	public SybaseSchema(JdbcTemplate jdbcTemplate, SybaseDbSupport dbSupport,
			String name) {
		super(jdbcTemplate, dbSupport, name);
	}

	@Override
	protected boolean doExists() throws SQLException {
		return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM sysusers where name = ?", name) > 0;
	}

	@Override
	protected boolean doEmpty() throws SQLException {
		return jdbcTemplate.queryForInt("select COUNT(*) from sysobjects so, sysusers su where so.uid = su.uid and su.name = ?", name) > 0;
	}

	@Override
	protected void doCreate() throws SQLException {
		jdbcTemplate.execute("CREATE USER " + dbSupport.quote(name) + " IDENTIFIED BY flyway");
        jdbcTemplate.execute("GRANT RESOURCE TO " + dbSupport.quote(name));
	}

	@Override
	protected void doDrop() throws SQLException {
		jdbcTemplate.execute("DROP USER " + dbSupport.quote(name));
	}

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
			result[i] = new SybaseTable(jdbcTemplate, dbSupport, this, tableName);
		}
		
		return result;
	}

	@Override
	public Table getTable(String tableName) {
		return new SybaseTable(jdbcTemplate, dbSupport, this, tableName);
	}
	
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
				sql = "drop table ?";
			} else if ("V".equals(sybaseObjType)) {
				sql = "drop view ?";
			} else if ("P".equals(sybaseObjType)) {
				//dropping stored procedure
				sql = "drop procedure ?";
			} else if ("TR".equals(sybaseObjType)) {
				sql = "drop trigger ?";
			}
			
			jdbcTemplate.execute(sql, name);
		}
	}

}
