/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.ingres;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ingres implementation of Schema.
 * 
 * Ingress only partially supports Schemas. Each user gets a schema named as the username.
 * 
 */
public class IngresSchema extends Schema {
    /**
     * Creates a new PostgreSQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public IngresSchema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM iischema WHERE schema_name=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM iitables WHERE user=? AND system_use='U'",
                name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        // Schemas not supported by Ingres
    }

    @Override
    protected void doDrop() throws SQLException {
        // Schemas not supported by Ingres
    }

    @Override
    protected void doClean() throws SQLException {
        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generates the statements for dropping the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        List<String> sequenceNames =
                jdbcTemplate.queryForStringList(
                        "SELECT seq_name FROM iisequences WHERE seq_owner=?", name);

        List<String> statements = new ArrayList<String>();
        for (String sequenceName : sequenceNames) {
            statements.add("DROP SEQUENCE " + dbSupport.quote(name, sequenceName));
        }

        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        //Search for all the table names
                        "SELECT t.table_name FROM iitables t" +
                                //in this schema
                                " WHERE user=? AND system_use='U'" +
                                //that are real tables (as opposed to views)
                                " AND table_type='T'",
                        name);
        //Views are excluded as they are dropped with the parent table when using cascade.

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new IngresTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new IngresTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
