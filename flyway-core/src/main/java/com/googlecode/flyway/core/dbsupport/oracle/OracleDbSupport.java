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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport implements DbSupport {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(OracleDbSupport.class);

    /**
     * The jdbcTemplate to use.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance.
     *
     * @param jdbcTemplate The jdbcTemplate to use.
     */
    public OracleDbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/oracle/";
    }

    @Override
    public String getCurrentUserFunction() {
        return "USER";
    }

    @Override
    public String getCurrentSchema() {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                return connection.getMetaData().getUserName();
            }
        });
    }

    @Override
    public boolean isSchemaEmpty() {
        int objectCount = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");
        return objectCount == 0;
    }

    @Override
    public boolean tableExists(final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, getCurrentSchema(),
                        table.toUpperCase(), null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean columnExists(final String table, final String column) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getColumns(null, getCurrentSchema(),
                        table.toUpperCase(), column.toUpperCase());
                return resultSet.next();
            }
        });
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
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new OracleSqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript() {
        final List<String> allDropStatements = new ArrayList<String>();
        allDropStatements.add("PURGE RECYCLEBIN");
        allDropStatements.addAll(generateDropStatementsForSpatialExtensions());
        allDropStatements.addAll(generateDropStatementsForObjectType("SEQUENCE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType("FUNCTION", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType("MATERIALIZED VIEW", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType("PACKAGE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType("PROCEDURE", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType("SYNONYM", ""));
        allDropStatements.addAll(generateDropStatementsForObjectType("VIEW", "CASCADE CONSTRAINTS"));
        allDropStatements.addAll(generateDropStatementsForObjectType("TABLE", "CASCADE CONSTRAINTS PURGE"));
        allDropStatements.addAll(generateDropStatementsForObjectType("TYPE", ""));

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
     * @param objectType     The type of database object to drop.
     * @param extraArguments The extra arguments to add to the drop statement.
     *
     * @return The complete drop statements, ready to execute.
     */
    @SuppressWarnings({"unchecked"})
    private List<String> generateDropStatementsForObjectType(String objectType, final String extraArguments) {
        String query = "SELECT object_type, object_name FROM user_objects WHERE object_type = ?" +
                // Ignore Recycle bin objects
                " and object_name not like 'BIN$%'";

        if (!isOracleXE()) {
            // Ignore Spatial Index Tables as they get dropped automatically when the parent table gets dropped.
            // The automatic drop does not work on XE.
            query += " and object_name not like 'MDRT_%$'";
        }

        return jdbcTemplate.query(query,
                new Object[]{objectType}, new RowMapper() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return "DROP " + rs.getString("OBJECT_TYPE") + " \"" + rs.getString("OBJECT_NAME") + "\" " + extraArguments;
                    }
                });
    }

    /**
     * Generates the drop statements for Oracle Spatial Extensions-related database objects.
     *
     * @return The complete drop statements, ready to execute.
     */
    private List<String> generateDropStatementsForSpatialExtensions() {
        List<String> statements = new ArrayList<String>();

        if (spatialExtensionsAvailable()) {
            statements.add("DELETE FROM mdsys.user_sdo_geom_metadata");
            if (isOracleXE()) {
                String user = getCurrentSchema();
                statements.add("DELETE FROM mdsys.sdo_index_metadata_table WHERE sdo_index_owner = '" + user + "'");
            }
        } else {
            LOG.debug("Oracle Spatial Extensions are not available. No cleaning of MDSYS tables and views.");
        }

        return statements;
    }

    /**
     * Checks whether Oracle Spatial extensions are available or not.
     *
     * @return {@code true} if they are available, {@code false} if not.
     */
    private boolean spatialExtensionsAvailable() {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE owner = 'MDSYS' AND view_name = 'USER_SDO_GEOM_METADATA'") > 0;
    }

    /**
     * Checks whether we are connected to Oracle XE or a full-blown edition.
     *
     * @return {@code true} if it is XE, {@code false} if not.
     */
    private boolean isOracleXE() {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                if (databaseMetaData == null) {
                    throw new FlywayException("Unable to read database metadata while it is null!");
                }
                return databaseMetaData.getDatabaseProductVersion().contains("Express Edition");
            }
        });
    }
}
