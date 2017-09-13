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

import java.sql.SQLException;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */
public class Neo4JTable extends Table{

	public Neo4JTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
		super(jdbcTemplate, dbSupport, schema, name);
	}

	@Override
	protected boolean doExists() throws SQLException {
		return true; 
	}

	@Override
	protected void doLock() throws SQLException {
	}

	@Override
	protected void doDrop() throws SQLException {
		jdbcTemplate.execute("MATCH n = (:schema_version)-->() DELETE n");
	}
	
	@Override
	public boolean hasColumn(String column) {
		try {
			return jdbcTemplate.queryForInt("MATCH (n:Migration) WHERE NOT n." + column + " IS NULL RETURN COUNT(*)") != 0;
		} catch (SQLException e) {
			return false;
		}
	}

}
