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
package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

/**
 * Redshift implementation of Schema.
 */
public class RedshiftSchema extends Schema<RedshiftDbSupport> {
    /**
     * Creates a new Redshift schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public RedshiftSchema(JdbcTemplate jdbcTemplate, RedshiftDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        for (Table table : allTables()) {
            table.drop();
        }

        //Drop views that are based on a system table (views are normally dropped with the parent table when using cascade)
        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(statement);
        }

        // Custom sequences, functions, domains and types are not supported by Redshift.
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        //Search for all the table names
                        "SELECT t.table_name FROM information_schema.tables t" +
                                //in this schema
                                " WHERE table_schema=?" +
                                //that are real tables (as opposed to views)
                                " AND table_type = 'BASE TABLE'",
                                //No need for further predicates, since Redshift does not support inheritance
                        name);
        //Child tables are dropped with the parent table when using cascade

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new RedshiftTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    protected List<String> generateDropStatementsForViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        //Search for all the table names
                        "SELECT t.table_name FROM information_schema.tables t" +
                                //in this schema
                                " WHERE table_schema=?" +
                                //that are real tables (as opposed to views)
                                " AND table_type = 'VIEW'",
                                //No need for further predicates, since Redshift does not support inheritance
                        name);
        //Views are normally dropped with the parent table when using cascade, unless the view is based on a system table

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW " + dbSupport.quote(name, viewName) + " CASCADE");
        }
        return statements;
    }

    @Override
    public Table getTable(String tableName) {
        return new RedshiftTable(jdbcTemplate, dbSupport, this, tableName);
    }

}
