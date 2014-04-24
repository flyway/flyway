/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.dbsupport.redshift;

import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.dbsupport.DbSupport;
import org.flywaydb.core.dbsupport.JdbcTemplate;
import org.flywaydb.core.dbsupport.Schema;
import org.flywaydb.core.dbsupport.Table;

/**
 * Redshift implementation of Schema.
 */
public class RedshiftSchema extends Schema
{
    /**
     * Creates a new Redshift schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public RedshiftSchema(JdbcTemplate jdbcTemplate,
                          DbSupport dbSupport,
                          String name)
    {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException
    {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException
    {
        int objectCount = jdbcTemplate.queryForInt("SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                                                   name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException
    {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException
    {
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException
    {
        for (Table table : allTables()) {
            table.drop();
        }

        // Custom sequences, functions, domains and types are not supported by AWS Redshift.
    }

    @Override
    protected Table[] doAllTables() throws SQLException
    {
        List<String> tableNames = jdbcTemplate.queryForStringList(
        //Search for all the table names
        "SELECT t.table_name FROM information_schema.tables t"
                +
                //in this schema
                " WHERE table_schema=?"
                +
                //that are real tables (as opposed to views)
                " AND table_type='BASE TABLE'"
                +
                //and are not child tables (= do not inherit from another table).
                " AND NOT (SELECT EXISTS (SELECT inhrelid FROM pg_catalog.pg_inherits"
                + " WHERE inhrelid = (SELECT c.oid FROM pg_catalog.pg_class c"
                + " INNER JOIN pg_catalog.pg_namespace n ON (c.relnamespace = n.oid)"
                + " WHERE c.relname = t.table_name and n.nspname = t.table_schema)))",
                                                                  name);
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new RedshiftTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName)
    {
        return new RedshiftTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
