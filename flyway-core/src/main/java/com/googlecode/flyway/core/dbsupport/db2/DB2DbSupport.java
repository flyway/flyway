/**
 * Copyright (C) 2010-2011 the original author or authors.
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
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB2 Support.
 */
public class DB2DbSupport implements DbSupport {
    /**
     * The jdbcTemplate to use.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * @param jdbcTemplate to use
     */
    public DB2DbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @see com.googlecode.flyway.core.dbsupport.DbSupport#createSqlScript(java.lang.String,
     *      com.googlecode.flyway.core.migration.sql.PlaceholderReplacer)
     */
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
        List<String> dropStatements = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        List<String> dbObjects = jdbcTemplate.queryForList(query, String.class);
        for (String dbObject : dbObjects) {
            // DB2 needs double quotes
            dropStatements.add(dropPrefix + " \"" + schema + "\".\"" + dbObject + "\"");
        }
        return dropStatements;
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/db2/";
    }

    public boolean isSchemaEmpty(String schema) {
        int objectCount = jdbcTemplate
                .queryForInt("select count(*) from syscat.tables where tabschema = ?", new String[] {schema});
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.views where viewschema = ?", new String[] {schema});
        objectCount += jdbcTemplate
                .queryForInt("select count(*) from syscat.sequences where seqschema = ?", new String[] {schema});
        objectCount += jdbcTemplate.queryForInt("select count(*) from syscat.indexes where indschema = ?", new String[] {schema});
        return objectCount == 0;
    }

    public boolean tableExists(final String schema, final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, schema.toUpperCase(), table.toUpperCase(),
                        null);
                return resultSet.next();
            }
        });
    }

    public String getCurrentSchema() {
        return ((String) jdbcTemplate.queryForObject("select current_schema from sysibm.sysdummy1", String.class))
                .trim();
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) {
        jdbcTemplate.execute("lock table " + schema + "." + table + " in exclusive mode");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }
}
