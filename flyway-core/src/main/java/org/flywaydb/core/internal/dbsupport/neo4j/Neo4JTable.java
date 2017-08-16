package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.SQLException;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class Neo4JTable extends Table{

	public Neo4JTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
		super(jdbcTemplate, dbSupport, schema, name);
	}

	@Override
	protected boolean doExists() throws SQLException {
		return jdbcTemplate.queryForInt("MATCH n = (:SchemaVersion)-->() RETURN COUNT(n)")== 0;
	}

	@Override
	protected void doLock() throws SQLException {
		// TODO Auto-generated method stub investigate Neo4jLockSystem
		
	}

	@Override
	protected void doDrop() throws SQLException {
		jdbcTemplate.execute("MATCH n = (:SchemaVersion)-->() DELETE n");
	}

}
