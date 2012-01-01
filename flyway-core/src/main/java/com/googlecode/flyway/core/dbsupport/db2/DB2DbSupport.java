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
package com.googlecode.flyway.core.dbsupport.db2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB2 Support.
 */
public class DB2DbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DB2DbSupport(Connection connection) {
        super(new DB2JdbcTemplate(connection));
    }

    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new DB2SqlScript(sqlScriptSource, placeholderReplacer);
    }

    public SqlScript createCleanScript(String schema) {
        // TODO PROCEDURES and FUNCTIONS
        final List<String> allDropStatements = new ArrayList<String>();

        // views
        String dropViewsGenQuery = "select rtrim(VIEWNAME) from SYSCAT.VIEWS where VIEWSCHEMA = '" + schema
                + "'";
        List<String> dropViewsStatements = buildDropStatements("drop view", dropViewsGenQuery, schema);
        allDropStatements.addAll(dropViewsStatements);

        // tables
        String dropTablesGenQuery = "select rtrim(TABNAME) from SYSCAT.TABLES where TYPE='T' and TABSCHEMA = '" + schema
                + "'";
        List<String> dropTableStatements = buildDropStatements("drop table", dropTablesGenQuery, schema);
        allDropStatements.addAll(dropTableStatements);

        // sequences
        String dropSeqGenQuery = "select rtrim(SEQNAME) from SYSCAT.SEQUENCES where SEQSCHEMA = '" + schema
                + "' and SEQTYPE='S'";
        List<String> dropSeqStatements = buildDropStatements("drop sequence", dropSeqGenQuery, schema);
        allDropStatements.addAll(dropSeqStatements);

        // indices in DB2 are deleted, if the corresponding table is dropped

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int count = 0;
        for (String dropStatement : allDropStatements) {
            count++;
            sqlStatements.add(new SqlStatement(count, dropStatement));
        }

        return new SqlScript(sqlStatements);
    }

    /**
     * Builds the drop statements for database objects in this schema.
     *
     * @param dropPrefix The drop command for the database object (e.g. 'drop table').
     * @param query      The query to get all present database objects
     * @param schema     The schema for which to build the statements.
     * @return The statements.
     */
    private List<String> buildDropStatements(final String dropPrefix, final String query, String schema) {
        try {
            List<String> dropStatements = new ArrayList<String>();
            List<String> dbObjects = jdbcTemplate.queryForStringList(query);
            for (String dbObject : dbObjects) {
                // DB2 needs double quotes
                dropStatements.add(dropPrefix + " \"" + schema + "\".\"" + dbObject + "\"");
            }
            return dropStatements;
        } catch (SQLException e) {
            throw new FlywayException("Error building drop statements for schema " + schema, e);
        }
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/db2/";
    }

    public boolean isSchemaEmpty(String schema) {
        try {
            int objectCount = jdbcTemplate.queryForInt("select count(*) from syscat.tables where tabschema = ?", schema);
            objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.views where viewschema = ?", schema);
            objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.sequences where seqschema = ?", schema);
            objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.indexes where indschema = ?", schema);
            return objectCount == 0;
        } catch (SQLException e) {
            throw new FlywayException("Error checking whether schema '" + schema + "' is empty", e);
        }
    }

    public boolean tableExists(final String schema, final String table) {
        try {
            ResultSet resultSet = jdbcTemplate.getMetaData().getTables(null, schema.toUpperCase(), table.toUpperCase(),
                    null);
            return resultSet.next();
        } catch (SQLException e) {
            throw new FlywayException("Error while checking whether table exists: " + schema + "." + table, e);

        }
    }

    public String getCurrentSchema() {
        try {
            return jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1").trim();
        } catch (SQLException e) {
            throw new FlywayException("Error retrieving current schema", e);
        }
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.update("lock table " + schema + "." + table + " in exclusive mode");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }
}
