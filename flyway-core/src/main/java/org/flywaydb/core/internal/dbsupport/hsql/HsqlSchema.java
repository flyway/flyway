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
package org.flywaydb.core.internal.dbsupport.hsql;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hsql implementation of Schema.
 */
public class HsqlSchema extends Schema<HsqlDbSupport> {
    /**
     * Creates a new Hsql schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public HsqlSchema(JdbcTemplate jdbcTemplate, HsqlDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT (*) FROM information_schema.system_schemas WHERE table_schem=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        String user = jdbcTemplate.queryForString("SELECT USER() FROM (VALUES(0))");
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name) + " AUTHORIZATION " + user);
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

        for (String statement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generates the statements to drop the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        List<String> sequenceNames = jdbcTemplate.queryForStringList(
                "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES where SEQUENCE_SCHEMA = ?", name);

        List<String> statements = new ArrayList<String>();
        for (String seqName : sequenceNames) {
            statements.add("DROP SEQUENCE " + dbSupport.quote(name, seqName));
        }

        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = ? AND TABLE_TYPE = 'TABLE'", name);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new HsqlTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new HsqlTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
