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
package com.googlecode.flyway.core.dbsupport.timesten;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TimesTen implementation of Schema.
 */
public class TimesTenSchema extends Schema {
    private static final Log LOG = LogFactory.getLog(TimesTenSchema.class);

    /**
     * Creates a new TimesTen schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public TimesTenSchema(JdbcTemplate jdbcTemplate, DbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_users WHERE username=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(*) FROM all_objects WHERE owner = ?", name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE USER " + dbSupport.quote(name) + " IDENTIFIED BY flyway");
        jdbcTemplate.execute("GRANT RESOURCE TO " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP USER " + dbSupport.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        if ("SYSTEM".equals(name.toUpperCase())) {
            throw new FlywayException("Clean not supported on TimesTen for user 'SYSTEM'! You should NEVER add your own objects to the SYSTEM schema!");
        }

        for (String statement : generateDropStatementsForObjectType("SEQUENCE", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("FUNCTION", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("MATERIALIZED VIEW", "PRESERVE TABLE")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("PACKAGE", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("PROCEDURE", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("SYNONYM", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("TRIGGER", "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("VIEW", "CASCADE CONSTRAINTS")) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForXmlTables()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForObjectType("TYPE", "FORCE")) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generates the drop statements for all xml tables.
     *
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForXmlTables() throws SQLException {
        List<String> dropStatements = new ArrayList<String>();

        if (!xmlDBExtensionsAvailable()) {
            LOG.debug("TimesTen XML DB Extensions are not available. No cleaning of XML tables.");
            return dropStatements;
        }

        List<String> objectNames =
                jdbcTemplate.queryForStringList("SELECT table_name FROM all_xml_tables WHERE owner = ?", name);
        for (String objectName : objectNames) {
            dropStatements.add("DROP TABLE " + dbSupport.quote(name, objectName) + " PURGE");
        }
        return dropStatements;
    }

    /**
     * Checks whether TimesTen XML DB extensions are available or not.
     *
     * @return {@code true} if they are available, {@code false} if not.
     * @throws SQLException when checking availability of the extensions failed.
     */
    private boolean xmlDBExtensionsAvailable() throws SQLException {
        return (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_users WHERE username = 'XDB'") > 0)
                && (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE view_name = 'RESOURCE_VIEW'") > 0);
    }

    /**
     * Generates the drop statements for all database objects of this type.
     *
     * @param objectType     The type of database object to drop.
     * @param extraArguments The extra arguments to add to the drop statement.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForObjectType(String objectType, String extraArguments) throws SQLException {
        String query = "SELECT object_name FROM all_objects WHERE object_type = ? AND owner = ?";

        List<String> objectNames = jdbcTemplate.queryForStringList(query, objectType, name);
        List<String> dropStatements = new ArrayList<String>();
        for (String objectName : objectNames) {
            dropStatements.add("DROP " + objectType + " " + dbSupport.quote(name, objectName) + " " + extraArguments);
        }
        return dropStatements;
    }


    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM all_tables WHERE owner = ?"
                        + " AND nested != 'YES'"
                        + " AND secondary != 'Y'", name);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new TimesTenTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new TimesTenTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
