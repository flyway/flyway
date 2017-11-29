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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.CallableStatement;
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
 * Oracle database.
 */
public class OracleDatabase extends Database {
    private static final String ORACLE_NET_TNS_ADMIN = "oracle.net.tns_admin";

    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public OracleDatabase(FlywayConfiguration configuration, Connection connection
                          // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                          // [/pro]
    ) {
        super(configuration, connection, Types.VARCHAR
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );

        // If the TNS_ADMIN environment variable is set, enable tnsnames.ora support for the Oracle JDBC driver
        // See http://www.orafaq.com/wiki/TNS_ADMIN
        String tnsAdminEnvVar = System.getenv("TNS_ADMIN");
        String tnsAdminSysProp = System.getProperty(ORACLE_NET_TNS_ADMIN);
        if (StringUtils.hasLength(tnsAdminEnvVar) && tnsAdminSysProp == null) {
            System.setProperty(ORACLE_NET_TNS_ADMIN, tnsAdminEnvVar);
        }
    }

    @Override
    protected org.flywaydb.core.internal.database.Connection getConnection(Connection connection, int nullType
                                                                           // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                                                                           // [/pro]
    ) {
        return new OracleConnection(configuration, this, connection, nullType
                // [pro]
                , dryRunStatementInterceptor
                // [/pro]
        );
    }

    @Override
    protected final void ensureSupported() {
        int majorVersion = getMajorVersion();
        if (majorVersion < 10) {
            throw new FlywayDbUpgradeRequiredException("Oracle", "" + majorVersion, "10");
        }
        // [enterprise-not]
        //if (majorVersion == 10 || majorVersion == 11) {
        //throw new org.flywaydb.core.internal.exception.FlywayEnterpriseUpgradeRequiredException("Oracle", "Oracle", "" + majorVersion);
        //}
        // [/enterprise-not]
        if (majorVersion > 12) {
            recommendFlywayUpgrade("Oracle", "" + majorVersion);
        }
    }

    public String getDbName() {
        return "oracle";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return mainConnection.getJdbcTemplate().queryForString("SELECT USER FROM DUAL");
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
        return new OracleSqlStatementBuilder(getDefaultDelimiter());
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
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
    boolean queryReturnsRows(String query, String... params) throws SQLException {
        return mainConnection.getJdbcTemplate().queryForBoolean("SELECT CASE WHEN EXISTS(" + query + ") THEN 1 ELSE 0 END FROM DUAL", params);
    }

    /**
     * Checks whether the specified privilege or role is granted to the current user.
     *
     * @return {@code true} if it is granted, {@code false} if not.
     * @throws SQLException if the check failed.
     */
    boolean isPrivOrRoleGranted(String name) throws SQLException {
        return queryReturnsRows("SELECT 1 FROM SESSION_PRIVS WHERE PRIVILEGE = ? UNION ALL " +
                "SELECT 1 FROM SESSION_ROLES WHERE ROLE = ?", name, name);
    }

    /**
     * Checks whether the specified data dictionary view in the specified system schema is accessible (directly or
     * through a role) or not.
     *
     * @param owner the schema name, unquoted case-sensitive.
     * @param name  the data dictionary view name to check, unquoted case-sensitive.
     * @return {@code true} if it is accessible, {@code false} if not.
     * @throws SQLException if the check failed.
     */
    private boolean isDataDictViewAccessible(String owner, String name) throws SQLException {
        return queryReturnsRows("SELECT * FROM ALL_TAB_PRIVS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?" +
                " AND PRIVILEGE = 'SELECT'", owner, name);
    }

    /**
     * Checks whether the specified SYS view is accessible (directly or through a role) or not.
     *
     * @param name the data dictionary view name to check, unquoted case-sensitive.
     * @return {@code true} if it is accessible, {@code false} if not.
     * @throws SQLException if the check failed.
     */
    boolean isDataDictViewAccessible(String name) throws SQLException {
        return isDataDictViewAccessible("SYS", name);
    }

    /**
     * Returns the specified data dictionary view name prefixed with DBA_ or ALL_ depending on its accessibility.
     *
     * @param baseName the data dictionary view base name, unquoted case-sensitive, e.g. OBJECTS, TABLES.
     * @return the full name of the view with the proper prefix.
     * @throws SQLException if the check failed.
     */
    String dbaOrAll(String baseName) throws SQLException {
        return isPrivOrRoleGranted("SELECT ANY DICTIONARY") || isDataDictViewAccessible("DBA_" + baseName)
                ? "DBA_" + baseName
                : "ALL_" + baseName;
    }

    /**
     * Returns the set of Oracle options available on the target database.
     *
     * @return the set of option titles.
     * @throws SQLException if retrieving of options failed.
     */
    private Set<String> getAvailableOptions() throws SQLException {
        return new HashSet<String>(mainConnection.getJdbcTemplate()
                .queryForStringList("SELECT PARAMETER FROM V$OPTION WHERE VALUE = 'TRUE'"));
    }

    /**
     * Checks whether Flashback Data Archive option is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the feature failed.
     */
    boolean isFlashbackDataArchiveAvailable() throws SQLException {
        return getAvailableOptions().contains("Flashback Data Archive");
    }

    /**
     * Checks whether XDB component is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the component failed.
     */
    boolean isXmlDbAvailable() throws SQLException {
        return isDataDictViewAccessible("ALL_XML_TABLES");
    }

    /**
     * Checks whether Data Mining option is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the feature failed.
     */
    boolean isDataMiningAvailable() throws SQLException {
        return getAvailableOptions().contains("Data Mining");
    }

    /**
     * Checks whether Oracle Locator component is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the component failed.
     */
    boolean isLocatorAvailable() throws SQLException {
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
    Set<String> getSystemSchemas() throws SQLException {

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

        // [enterprise]
        // APEX has a schema with a different name for each version, so get it from ALL_USERS. In addition, starting
        // from Oracle 12.1, there is a special column in ALL_USERS that marks Oracle-maintained schemas.
        boolean oracle12cOrHigher = getMajorVersion() >= 12;
        // [/enterprise]
        result.addAll(mainConnection.getJdbcTemplate().queryForStringList("SELECT USERNAME FROM ALL_USERS " +
                        "WHERE REGEXP_LIKE(USERNAME, '^(APEX|FLOWS)_\\d+$')" +
                        // [enterprise]
                        (oracle12cOrHigher ?
                                // [/enterprise]
                                " OR ORACLE_MAINTAINED = 'Y'"
                                // [enterprise]
                                : "")
                // [/enterprise]
        ));

        // [enterprise]
        // For earlier Oracle versions check also DBA_REGISTRY if possible.
        if (!oracle12cOrHigher && isDataDictViewAccessible("DBA_REGISTRY")) {
            List<List<String>> schemaSuperList = mainConnection.getJdbcTemplate().query(
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
        // [/enterprise]

        return result;
    }

    @Override
    public List<String> getServerOutput() {
        List<String> result = new ArrayList<String>();

        CallableStatement stmt = null;
        try {
            stmt = getMigrationConnection().getJdbcConnection().prepareCall("BEGIN\ndbms_output.get_line(?,?);\nEND;");
            stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
            stmt.registerOutParameter(2, java.sql.Types.NUMERIC);

            int status;
            do {
                stmt.executeUpdate();
                String line = stmt.getString(1);
                if (line == null) line = "";
                status = stmt.getInt(2);
                if (status == 0) {
                    result.add(line);
                }
            } while (status == 0);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to get server output", e);
        } finally {
            JdbcUtils.closeStatement(stmt);
        }
        return result;
    }
}
