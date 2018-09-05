/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Derby implementation of Schema.
 */
public class DerbySchema extends Schema<DerbyDatabase> {
    /**
     * Creates a new Derby schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    public DerbySchema(JdbcTemplate jdbcTemplate, DerbyDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT (*) FROM sys.sysschemas WHERE schemaname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " RESTRICT");
    }

    @Override
    protected void doClean() throws SQLException {
        List<String> triggerNames = listObjectNames("TRIGGER", "");
        for (String statement : generateDropStatements("TRIGGER", triggerNames, "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForConstraints()) {
            jdbcTemplate.execute(statement);
        }

        List<String> viewNames = listObjectNames("TABLE", "TABLETYPE='V'");
        for (String statement : generateDropStatements("VIEW", viewNames, "")) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        List<String> sequenceNames = listObjectNames("SEQUENCE", "");
        for (String statement : generateDropStatements("SEQUENCE", sequenceNames, "RESTRICT")) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generate the statements for dropping all the constraints in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForConstraints() throws SQLException {
        List<Map<String, String>> results = jdbcTemplate.queryForList("SELECT c.constraintname, t.tablename FROM sys.sysconstraints c" +
                " INNER JOIN sys.systables t ON c.tableid = t.tableid" +
                " INNER JOIN sys.sysschemas s ON c.schemaid = s.schemaid" +
                " WHERE c.type = 'F' AND s.schemaname = ?", name);

        List<String> statements = new ArrayList<>();
        for (Map<String, String> result : results) {
            String dropStatement = "ALTER TABLE " + database.quote(name, result.get("TABLENAME"))
                    + " DROP CONSTRAINT " + database.quote(result.get("CONSTRAINTNAME"));

            statements.add(dropStatement);
        }
        return statements;
    }

    /**
     * Generate the statements for dropping all the objects of this type in this schema.
     *
     * @param objectType          The type of object to drop (Sequence, constant, ...)
     * @param objectNames         The names of the objects to drop.
     * @param dropStatementSuffix Suffix to append to the statement for dropping the objects.
     * @return The list of statements.
     */
    private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix) {
        List<String> statements = new ArrayList<>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + " " + database.quote(name, objectName) + " " + dropStatementSuffix;

            statements.add(dropStatement);
        }
        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = listObjectNames("TABLE", "TABLETYPE='T'");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DerbyTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    /**
     * List the names of the objects of this type in this schema.
     *
     * @param objectType  The type of objects to list (Sequence, constant, ...)
     * @param querySuffix Suffix to append to the query to find the objects to list.
     * @return The names of the objects.
     * @throws SQLException when the object names could not be listed.
     */
    private List<String> listObjectNames(String objectType, String querySuffix) throws SQLException {
        String query = "SELECT " + objectType + "name FROM sys.sys" + objectType + "s WHERE schemaid in (SELECT schemaid FROM sys.sysschemas where schemaname = ?)";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForStringList(query, name);
    }

    @Override
    public Table getTable(String tableName) {
        return new DerbyTable(jdbcTemplate, database, this, tableName);
    }
}