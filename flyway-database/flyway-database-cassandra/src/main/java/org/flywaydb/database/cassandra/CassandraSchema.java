/*-
 * ========================LICENSE_START=================================
 * flyway-database-cassandra
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.cassandra;

import java.util.ArrayList;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

public class CassandraSchema extends Schema<CassandraDatabase, CassandraTable> {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public CassandraSchema(final JdbcTemplate jdbcTemplate, final CassandraDatabase database, final String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) from system_schema.keyspaces where keyspace_name=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE KEYSPACE IF NOT EXISTS "
            + database.quote(name)
            + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : '1' };");
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP KEYSPACE " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        if (isSystem()) {
            throw new FlywayException("Clean not supported for system schemas " + database.quote(name) + "!");
        }

        for (final String statement : dropIndexes()) {
            jdbcTemplate.execute(statement);
        }

        for (final String statement : dropViews()) {
            jdbcTemplate.execute(statement);
        }

        for (final String statement : dropAggregates()) {
            jdbcTemplate.execute(statement);
        }

        for (final String statement : dropFunctions()) {
            jdbcTemplate.execute(statement);
        }

        for (final Table table : allTables()) {
            table.drop(); //also drops triggers
        }

        for (final String statement : dropTypes()) {
            jdbcTemplate.execute(statement);
        }
    }

    private List<String> dropIndexes() throws SQLException {
        final List<String> indexNames = jdbcTemplate.queryForStringList(
            "select index_name from system_schema.indexes where keyspace_name=?",
            name);
        final List<String> statements = new ArrayList<>();

        for (final String indexName : indexNames) {
            statements.add("drop index " + database.quote(name, indexName));
        }

        return statements;
    }

    private List<String> dropViews() throws SQLException {
        final List<String> viewNames = jdbcTemplate.queryForStringList(
            "select view_name from system_schema.views where keyspace_name=?",
            name);
        final List<String> statements = new ArrayList<>();

        for (final String viewName : viewNames) {
            statements.add("DROP MATERIALIZED VIEW " + database.quote(name, viewName));
        }

        return statements;
    }

    private List<String> dropFunctions() throws SQLException {
        final List<String> functionNames = jdbcTemplate.queryForStringList(
            "select function_name from system_schema.functions where keyspace_name=?",
            name);
        final List<String> statements = new ArrayList<>();

        for (final String functionName : functionNames) {
            statements.add("DROP FUNCTION " + database.quote(name, functionName));
        }

        return statements;
    }

    private List<String> dropAggregates() throws SQLException {
        final List<String> aggregateNames = jdbcTemplate.queryForStringList(
            "select aggregate_name from system_schema.aggregates where keyspace_name=?",
            name);
        final List<String> statements = new ArrayList<>();

        for (final String aggregateName : aggregateNames) {
            statements.add("DROP AGGREGATE " + database.quote(name, aggregateName));
        }

        return statements;
    }

    private List<String> dropTypes() throws SQLException {
        final List<String> typeNames = jdbcTemplate.queryForStringList(
            "select type_name from system_schema.types where keyspace_name = ?",
            name);
        final List<String> statements = new ArrayList<>();

        for (final String typeName : typeNames) {
            statements.add("DROP TYPE " + database.quote(name, typeName));
        }

        return statements;
    }

    @Override
    protected CassandraTable[] doAllTables() throws SQLException {
        final List<String> tableNames = jdbcTemplate.queryForStringList(
            "select table_name from system_schema.tables where keyspace_name=?",
            name);
        return tableNames.stream()
            .map(tableName -> new CassandraTable(jdbcTemplate, database, this, tableName))
            .toArray(CassandraTable[]::new);
    }

    @Override
    public Table getTable(final String tableName) {
        return new CassandraTable(jdbcTemplate, database, this, tableName);
    }

    private boolean isSystem() {
        return database.getSystemSchemas().contains(name);
    }
}
