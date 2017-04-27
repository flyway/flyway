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
package org.flywaydb.core.internal.dbsupport.oracle;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public OracleDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    public String getDbName() {
        return "oracle";
    }

    public String getCurrentUserFunction() {
        return "USER";
    }

    /**
     * Retrieves the current user.
     *
     * @return The current user for this connection.
     */
    public String getCurrentUserName() {
        try {
            return jdbcTemplate.queryForString("SELECT USER FROM DUAL");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the current user for the connection", e);
        }
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA=" + quote(schema));
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new OracleSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new OracleSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    /**
     * Checks whether the specified query returns rows or not. Wraps the query in EXISTS() SQL function and executes it.
     * This is more preferable to opening a cursor for the original query, because a query inside EXISTS() is implicitly
     * optimized to return the first row and because the client never fetches more than 1 row despite the fetch size
     * value.
     *
     * @param query  The query to check.
     * @param params The query parameters.
     * @return {@code true} if the query returns rows, {@code false} if not.
     * @throws SQLException when the query execution failed.
     */
    public boolean queryReturnsRows(String query, String... params) throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT CASE WHEN EXISTS(" + query + ") THEN 1 ELSE 0 END FROM DUAL", params);
    }

    /**
     * Checks whether DBA_ static data dictionary is accessible or not. This simply checks if the current user has
     * privileges to see the whole DBA dictionary, not some particular views.
     *
     * @return {@code true} if it is accessible, {@code false} if not.
     */
    public boolean isDbaDataDictAccessible() throws SQLException {
        return queryReturnsRows("SELECT 1 FROM SESSION_PRIVS WHERE PRIVILEGE = 'SELECT ANY DICTIONARY' UNION ALL " +
                "SELECT 1 FROM SESSION_ROLES WHERE ROLE = 'SELECT_CATALOG_ROLE'");
    }

    /**
     * Checks whether the specified data dictionary view in the specified system schema is accessible (directly or
     * through a role) or not.
     *
     * @param owner the schema name, unquoted case-sensitive.
     * @param name  the data dictionary view name to check, unquoted case-sensitive.
     * @return {@code true} if it is accessible, {@code false} if not.
     */
    public boolean isDataDictViewAccessible(String owner, String name) throws SQLException {
        return queryReturnsRows("SELECT * FROM ALL_TAB_PRIVS WHERE TABLE_SCHEMA = ? AND TABLE_NAME= ?" +
                " AND PRIVILEGE = 'SELECT'", owner, name);
    }

    /**
     * Checks whether the specified SYS view is accessible (directly or through a role) or not.
     *
     * @param name the data dictionary view name to check, unquoted case-sensitive.
     * @return {@code true} if it is accessible, {@code false} if not.
     */
    public boolean isDataDictViewAccessible(String name) throws SQLException {
        return isDataDictViewAccessible("SYS", name);
    }

    /**
     * Returns the set of Oracle options available on the target database.
     * @return the set of option titles.
     * @throws SQLException if retrieving of options failed.
     */
    public Set<String> getAvailableOptions() throws SQLException {
        return new HashSet<String>(jdbcTemplate.queryForStringList("SELECT PARAMETER FROM V$OPTION WHERE VALUE = 'TRUE'"));
    }

    /**
     * Checks whether Flashback Data Archive option is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the feature failed.
     */
    public boolean isFlashbackDataArchiveAvailable() throws SQLException {
        return getAvailableOptions().contains("Flashback Data Archive");
    }

    /**
     * Checks whether XDB component is available or not.
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the component failed.
     */
    public boolean isXmlDbAvailable() throws SQLException {
        return isDataDictViewAccessible("ALL_XML_TABLES");
    }

    /**
     * Checks whether Oracle Locator component is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the component failed.
     */
    public boolean isLocatorAvailable() throws SQLException {
        return isDataDictViewAccessible("MDSYS", "ALL_SDO_GEOM_METADATA");
    }

    /**
     * Returns the list of schemas that were created and are maintained by Oracle-supplied scripts and must not be
     * changed in any other way. The list is composed of default schemas mentioned in the official documentation for
     * Oracle Database versions from 10.1 to 12.2, and is dynamically extended with schemas from DBA_REGISTRY and
     * ALL_USERS (marked with ORACLE_MAINTAINED = 'Y' in Oracle 12c).
     *
     * @return the set of system schema names
     */
    public Set<String> getSystemSchemas() throws SQLException {

        // The list of known default system schemas
        Set<String> result = new HashSet<String>(Arrays.asList(
                "SYS", "SYSTEM", // Standard system accounts
                "SYSBACKUP", "SYSDG", "SYSKM", "SYSRAC", "SYS$UMF", // Auxiliary system accounts
                "DBSNMP", "MGMT_VIEW", "SYSMAN", // Enterprise Manager accounts
                "OUTLN", // Stored outlines
                "AUDSYS", // Unified auditing
                "ORACLE_OCM", // Oracle Configuration Manager
                "APPQOSSYS", // Oracle Database QoS Management
                "OJVMSYS", // Oracle JavaVM
                "DVF", "DVSYS", // Oracle Database Vault
                "DBSFWUSER", // Database Service Firewall
                "REMOTE_SCHEDULER_AGENT", // Remote scheduler agent
                "DIP", // Oracle Directory Integration Platform
                "APEX_PUBLIC_USER", "FLOWS_FILES", /*"APEX_######", "FLOWS_######",*/ // Oracle Application Express
                "ANONYMOUS", "XDB", "XS$NULL", // Oracle XML Database
                "CTXSYS", // Oracle Text
                "LBACSYS", // Oracle Label Security
                "EXFSYS", // Oracle Rules Manager and Expression Filter
                "MDDATA", "MDSYS", "SPATIAL_CSW_ADMIN_USR", "SPATIAL_WFS_ADMIN_USR", // Oracle Locator and Spatial
                "ORDDATA", "ORDPLUGINS", "ORDSYS", "SI_INFORMTN_SCHEMA", // Oracle Multimedia
                "WMSYS", // Oracle Workspace Manager
                "OLAPSYS", // Oracle OLAP catalogs
                "OWBSYS", "OWBSYS_AUDIT", // Oracle Warehouse Builder
                "GSMADMIN_INTERNAL", "GSMCATUSER", "GSMUSER", // Global Data Services
                "GGSYS", // Oracle GoldenGate
                "WK_TEST", "WKSYS", "WKPROXY", // Oracle Ultra Search
                "ODM", "ODM_MTR", "DMSYS", // Oracle Data Mining
                "TSMSYS" // Transparent Session Migration
        ));

        // APEX has a schema with a different name for each version, so get it from ALL_USERS. In addition, starting
        // from Oracle 12.1, there is a special column in ALL_USERS that marks Oracle-maintained schemas.
        boolean oracle12cOrHigher = jdbcTemplate.getMetaData().getDatabaseMajorVersion() >= 12;
        result.addAll(jdbcTemplate.queryForStringList("SELECT USERNAME FROM ALL_USERS " +
                "WHERE REGEXP_LIKE(USERNAME, '^(APEX|FLOWS)_\\d+$')" +
                (oracle12cOrHigher ? " OR ORACLE_MAINTAINED = 'Y'" : "")));

        // For earlier Oracle versions check also DBA_REGISTRY if possible.
        if (!oracle12cOrHigher && isDataDictViewAccessible("DBA_REGISTRY")) {
            List<List<String>> schemaSuperList = jdbcTemplate.query(
                    "SELECT SCHEMA, OTHER_SCHEMAS FROM DBA_REGISTRY",
                    new RowMapper<List<String>>() {
                        @Override
                        public List<String> mapRow(ResultSet rs) throws SQLException {
                            List<String> schemaList = new ArrayList<String>();
                            schemaList.add(rs.getString("SCHEMA"));
                            String otherSchemas = rs.getString("OTHER_SCHEMAS");
                            if (otherSchemas != null && !otherSchemas.trim().isEmpty()) {
                                schemaList.addAll(Arrays.asList(otherSchemas.trim().split("\\s*,\\s*")));
                            }
                            return schemaList;
                        }
                    });
            for (List<String> schemaList : schemaSuperList) {
                result.addAll(schemaList);
            }
        }

        return result;
    }

}
