/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * H2 implementation of Schema.
 */
public class H2Schema extends Schema {
    private static final Log LOG = LogFactory.getLog(H2Schema.class);

    /**
     * Creates a new H2 schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public H2Schema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    public boolean exists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?", name) > 0;
    }

    public boolean empty() throws SQLException {
        return allTables().length == 0;
    }

    public void create() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    public void drop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name));
    }

    public void clean() throws SQLException {
        for (Table table : allTables()) {
            table.drop();
        }

        List<String> sequenceNames = listObjectNames("SEQUENCE", "IS_GENERATED = false");
        for (String statement : generateDropStatements("SEQUENCE", sequenceNames, "")) {
            jdbcTemplate.execute(statement);
        }

        List<String> constantNames = listObjectNames("CONSTANT", "");
        for (String statement : generateDropStatements("CONSTANT", constantNames, "")) {
            jdbcTemplate.execute(statement);
        }

        List<String> domainNames = listObjectNames("DOMAIN", "");
        if (!domainNames.isEmpty()) {
            if (name.equals(dbSupport.getCurrentSchema().getName())) {
                for (String statement : generateDropStatementsForCurrentSchema("DOMAIN", domainNames, "")) {
                    jdbcTemplate.execute(statement);
                }
            } else {
                LOG.error("Unable to drop DOMAIN objects in schema " + dbSupport.quote(name)
                        + " due to H2 bug! (More info: http://code.google.com/p/h2database/issues/detail?id=306)");
            }
        }
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
        List<String> statements = new ArrayList<String>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + dbSupport.quote(name, objectName) + " " + dropStatementSuffix;

            statements.add(dropStatement);
        }
        return statements;
    }

    /**
     * Generate the statements for dropping all the objects of this type in the current schema.
     *
     * @param objectType          The type of object to drop (Sequence, constant, ...)
     * @param objectNames         The names of the objects to drop.
     * @param dropStatementSuffix Suffix to append to the statement for dropping the objects.
     * @return The list of statements.
     */
    private List<String> generateDropStatementsForCurrentSchema(String objectType, List<String> objectNames, String dropStatementSuffix) {
        List<String> statements = new ArrayList<String>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + dbSupport.quote(objectName) + " " + dropStatementSuffix;

            statements.add(dropStatement);
        }
        return statements;
    }

    @Override
    public Table[] allTables() throws SQLException {
        List<String> tableNames = listObjectNames("TABLE", "TABLE_TYPE = 'TABLE'");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new H2Table(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    /**
     * List the names of the objects of this type in this schema.
     *
     * @param objectType  The type of objects to list (Sequence, constant, ...)
     * @param querySuffix Suffix to append to the query to find the objects to list.
     * @return The names of the objects.
     * @throws java.sql.SQLException when the object names could not be listed.
     */
    private List<String> listObjectNames(String objectType, String querySuffix) throws SQLException {
        String query = "SELECT " + objectType + "_NAME FROM information_schema." + objectType + "s WHERE " + objectType + "_schema = ?";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForStringList(query, name);
    }


    @Override
    public Table getTable(String tableName) {
        return new H2Table(jdbcTemplate, dbSupport, this, tableName);
    }
}
