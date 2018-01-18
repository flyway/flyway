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
import java.util.List;
import java.util.concurrent.Callable;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */
public class Neo4JSchema extends Schema<DbSupport> {
	private static final Log LOG = LogFactory.getLog(Neo4JSchema.class);

	public Neo4JSchema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
		super(jdbcTemplate, dbSupport, name);

	}

	@Override
	protected boolean doExists() throws SQLException {
		return name.equals("No Database Schema");
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
        new TransactionTemplate(jdbcTemplate.getConnection()).execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LOG.info("Dropping existing nodes...");
                jdbcTemplate.queryForString("MATCH (n) DETACH DELETE n");
                return null;
            }
        });
		new TransactionTemplate(jdbcTemplate.getConnection()).execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<String> constraints = jdbcTemplate.queryForStringList("CALL db.constraints");
                for (String constraint : constraints) {
                    LOG.info("Dropping existing constraint: " + constraint);
                    jdbcTemplate.executeStatement(String.format("DROP %s", constraint));
                }
                return null;
            }
        });
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
