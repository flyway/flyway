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
package org.flywaydb.core.internal.dbsupport.saphana;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SAP HANA implementation of Schema.
 */
public class SapHanaSchema extends Schema<SapHanaDbSupport> {
    /**
     * Creates a new SAP HANA schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public SapHanaSchema(JdbcTemplate jdbcTemplate, SapHanaDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYS.SCHEMAS WHERE SCHEMA_NAME=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("select count(*) from sys.tables where schema_name = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sys.views where schema_name = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sys.sequences where schema_name = ?", name);
        objectCount += jdbcTemplate.queryForInt("select count(*) from sys.synonyms where schema_name = ?", name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name) + " RESTRICT");
    }

    @Override
    protected void doClean() throws SQLException {
        for (String dropStatement : generateDropStatements("SYNONYM")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("VIEW")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String dropStatement : generateDropStatements("SEQUENCE")) {
            jdbcTemplate.execute(dropStatement);
        }
    }

    /**
     * Generates DROP statements for this type of object in this schema.
     *
     * @param objectType The type of object.
     * @return The drop statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatements(String objectType) throws SQLException {
        List<String> dropStatements = new ArrayList<String>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(
                "select " +objectType + "_NAME from SYS." + objectType +"S where SCHEMA_NAME = '" + name + "'");
        for (String dbObject : dbObjects) {
            dropStatements.add("DROP " + objectType + " " + dbSupport.quote(name, dbObject) + " CASCADE");
        }
        return dropStatements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("select TABLE_NAME from SYS.TABLES where SCHEMA_NAME = ?", name);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SapHanaTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SapHanaTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
