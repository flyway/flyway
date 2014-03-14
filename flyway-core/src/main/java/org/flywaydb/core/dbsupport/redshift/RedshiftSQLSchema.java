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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.dbsupport.DbSupport;
import org.flywaydb.core.dbsupport.JdbcTemplate;
import org.flywaydb.core.dbsupport.Table;
import org.flywaydb.core.dbsupport.Type;
import org.flywaydb.core.dbsupport.postgresql.PostgreSQLSchema;

public class RedshiftSQLSchema extends PostgreSQLSchema
{
    /**
     * Creates a new RedshiftSQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public RedshiftSQLSchema(JdbcTemplate jdbcTemplate,
                             DbSupport dbSupport, String name)
    {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected Table[] doAllTables() throws SQLException
    {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        //Search for all the table names
                        "SELECT t.table_name FROM information_schema.tables t" +
                                //in this schema
                                " WHERE table_schema=?" +
                                //that are real tables (as opposed to views)
                                " AND table_type='BASE TABLE'" +
                                //and are not child tables (= do not inherit from another table).
                                " AND NOT (SELECT EXISTS (SELECT inhrelid FROM pg_catalog.pg_inherits" +
                                " WHERE inhrelid = (SELECT c.oid FROM pg_catalog.pg_class c" +
                                " INNER JOIN pg_catalog.pg_namespace n ON (c.relnamespace = n.oid)" +
                                " WHERE c.relname = t.table_name and n.nspname = t.table_schema)))",
                        name);
        //Views and child tables are excluded as they are dropped with the parent table when using cascade.

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new RedshiftSQLTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected void doClean() throws SQLException {
        for (Table table : allTables()) {
            table.drop();
        }

        // The following commented parts are not supported by AWS Redshift

        // for (String statement : generateDropStatementsForSequences()) {
        //     jdbcTemplate.execute(statement);
        // }


        // for (String statement : generateDropStatementsForBaseTypes(true)) {
        //     jdbcTemplate.execute(statement);
        // }

        for (String statement : generateDropStatementsForAggregates()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForRoutines()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForEnums()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForDomains()) {
            jdbcTemplate.execute(statement);
        }

        // for (String statement : generateDropStatementsForBaseTypes(false)) {
        //     jdbcTemplate.execute(statement);
        // }

        for (Type type : allTypes()) {
            type.drop();
        }
    }

    /**
     * Generates the statements for dropping the aggregates in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForAggregates() throws SQLException {
        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) "
                                + "WHERE pg_proc.proisagg = true AND ns.nspname = ?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP AGGREGATE IF EXISTS " + dbSupport.quote(name, row.get("proname")) + "(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the routines in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForRoutines() throws SQLException {
        List<Map<String, String>> rows =
                jdbcTemplate.queryForList(
                        "SELECT proname, oidvectortypes(proargtypes) AS args "
                                + "FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) "
                                + "WHERE pg_proc.proisagg = false AND ns.nspname = ?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION IF EXISTS " + dbSupport.quote(name, row.get("proname")) + "(" + row.get("args") + ") CASCADE");
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the enums in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForEnums() throws SQLException {
        List<String> enumNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.typname FROM pg_catalog.pg_type t INNER JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace WHERE n.nspname = ? and t.typtype = 'e'", name);

        List<String> statements = new ArrayList<String>();
        for (String enumName : enumNames) {
            statements.add("DROP TYPE " + dbSupport.quote(name, enumName));
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the domains in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForDomains() throws SQLException {
        List<String> domainNames =
                jdbcTemplate.queryForStringList(
                        "SELECT domain_name FROM information_schema.domains WHERE domain_schema=?", name);

        List<String> statements = new ArrayList<String>();
        for (String domainName : domainNames) {
            statements.add("DROP DOMAIN " + dbSupport.quote(name, domainName));
        }

        return statements;
    }

    @Override
    public Table getTable(String tableName)
    {
        return new RedshiftSQLTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
