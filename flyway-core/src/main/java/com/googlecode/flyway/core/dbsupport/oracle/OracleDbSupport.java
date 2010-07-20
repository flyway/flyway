/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.runtime.SqlScript;
import com.googlecode.flyway.core.runtime.SqlStatement;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport implements DbSupport {
    @Override
    public String getCreateMetaDataTableScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/oracle/createMetaDataTable.sql";
    }

    @Override
    public String getCurrentSchema(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getMetaData().getUserName();
            }
        });
    }

    @Override
    public boolean supportsDatabase(String databaseProductName) {
        return "Oracle".equals(databaseProductName);
    }

    @Override
    public boolean metaDataTableExists(JdbcTemplate jdbcTemplate, String schemaMetaDataTable) {
        int count = jdbcTemplate.queryForInt("SELECT count(*) FROM user_tables WHERE table_name = ?",
                new Object[]{schemaMetaDataTable.toUpperCase()});
        return count > 0;
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsLocking() {
        return true;
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, Map<String, String> placeholders) {
        return new OracleSqlScript(sqlScriptSource, placeholders);
    }

    @Override
    public SqlScript createCleanScript(JdbcTemplate jdbcTemplate) {
        final List<String> allDropStatements = new ArrayList<String>();
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "SEQUENCE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "FUNCTION", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "MATERIALIZED VIEW", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "PACKAGE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "PROCEDURE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "SYNONYM", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "TABLE", "CASCADE CONSTRAINTS PURGE"));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "TYPE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType(jdbcTemplate, "VIEW", ""));
        allDropStatements.addAll(generateDropStatementsForSpatialExtensions(jdbcTemplate));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int count = 0;
        for (String dropStatement : allDropStatements) {
            count++;
            sqlStatements.add(new SqlStatement(count, dropStatement));
        }

        return new SqlScript(sqlStatements);
    }

    /**
     * Generates the drop statements for all database objects of this type.
     *
     * @param jdbcTemplate   The jdbc template to use to query the database.
     * @param objectType     The type of database object to drop.
     * @param extraArguments The extra arguments to add to the drop statement.
     * @return The complete drop statements, ready to execute.
     */
    @SuppressWarnings({"unchecked"})
    private List<String> generateDropStatementsForObjectType(JdbcTemplate jdbcTemplate, String objectType, final String extraArguments) {
        return jdbcTemplate.query("SELECT object_type, object_name FROM user_objects WHERE object_type = ?",
                new Object[]{objectType}, new RowMapper() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return "DROP " + rs.getString("OBJECT_TYPE") + " " + rs.getString("OBJECT_NAME") + " " + extraArguments;
                    }
                });
    }

    /**
     * Generates the drop statements for Oracle Spatial Extensions-related database objects.
     *
     * @param jdbcTemplate   The jdbc template to use to query the database.
     * @return The complete drop statements, ready to execute.
     */
    @SuppressWarnings({"unchecked"})
    private List<String> generateDropStatementsForSpatialExtensions(JdbcTemplate jdbcTemplate) {
        List<String> statements = new ArrayList<String>();

        String user = getCurrentSchema(jdbcTemplate);
        statements.add("DELETE FROM mdsys.sdo_geom_metadata_table WHERE sdo_owner = '" + user + "'");
        statements.add("DELETE FROM mdsys.sdo_index_metadata_table WHERE sdo_index_owner = '" + user + "'");
        return statements;
    }
}
