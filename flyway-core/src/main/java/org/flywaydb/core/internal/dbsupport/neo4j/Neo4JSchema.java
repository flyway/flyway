package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class Neo4JSchema extends Schema<DbSupport> {
	private static final Log LOG = LogFactory.getLog(Neo4JSchema.class);

	public Neo4JSchema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
		super(jdbcTemplate, dbSupport, name);

	}

	@Override
	protected boolean doExists() throws SQLException {
		try {
			doAllTables();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	protected boolean doEmpty() throws SQLException {
		return jdbcTemplate.queryForInt("MATCH () RETURN COUNT (*)") == 0;
	}

	@Override
	protected void doCreate() throws SQLException {
		LOG.info("Neo4J does not support creating schemas. Schema not created: " + name);

	}

	@Override
	protected void doDrop() throws SQLException {
		LOG.info("Neo4J does not dropping creating schemas. Schema not created: " + name);

	}

	@Override
	protected void doClean() throws SQLException {
		jdbcTemplate.queryForString("MATCH n = ()-->() DELETE n");

	}

	@Override
	protected Table[] doAllTables() throws SQLException {
		List<String> tableNames = jdbcTemplate.queryForStringList(
				"MATCH (n)-[r]->() " + "UNWIND (labels(n) + type(r)) as tables" + " RETURN distinct tables");
		Table[] tables = new Table[tableNames.size()];
		for (int i = 0; i < tableNames.size(); i++) {
			tables[i] = new Neo4JTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
		}
		return tables;
	}

	@Override
	public Table getTable(String tableName) {
		return new Neo4JTable(jdbcTemplate, dbSupport, this, tableName);
	}

}
