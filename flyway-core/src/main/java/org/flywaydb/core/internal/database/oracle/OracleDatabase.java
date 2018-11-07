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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Oracle database.
 */
public class OracleDatabase extends Database<OracleConnection> {
    private static final String ORACLE_NET_TNS_ADMIN = "oracle.net.tns_admin";

    /**
     * If the TNS_ADMIN environment variable is set, enable tnsnames.ora support for the Oracle JDBC driver.
     * See http://www.orafaq.com/wiki/TNS_ADMIN
     */
    public static void enableTnsnamesOraSupport() {
        String tnsAdminEnvVar = System.getenv("TNS_ADMIN");
        String tnsAdminSysProp = System.getProperty(ORACLE_NET_TNS_ADMIN);
        if (StringUtils.hasLength(tnsAdminEnvVar) && tnsAdminSysProp == null) {
            System.setProperty(ORACLE_NET_TNS_ADMIN, tnsAdminEnvVar);
        }
    }








    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     */
    public OracleDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );










    }

    private String getConnectIdentifier() throws SQLException {
        String url = getJdbcMetaData().getURL();
        if (url == null) {
            return "";
        }
        return url.substring(url.indexOf("//") + 2);
    }

    @Override
    protected OracleConnection getConnection(Connection connection



    ) {
        return new OracleConnection(configuration, this, connection, originalAutoCommit



        );
    }

    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("Oracle", "10");

        ensureDatabaseIsCompatibleWithFlywayEdition("Oracle", "Oracle", "12.2");

        recommendFlywayUpgradeIfNecessary("Oracle", "18.0");
    }

    @Override
    protected SqlStatementBuilderFactory createSqlStatementBuilderFactory(
            PlaceholderReplacer placeholderReplacer



    ) {
        return new OracleSqlStatementBuilderFactory(placeholderReplacer




        );
    }

    @Override
    public SqlScriptExecutor createSqlScriptExecutor(JdbcTemplate jdbcTemplate



    ) {
        return new OracleSqlScriptExecutor(jdbcTemplate



        );
    }

    @Override
    protected PlaceholderReplacer createPlaceholderReplacer(boolean enabled, Map<String, String> placeholders,
                                                            String placeholderPrefix, String placeholderSuffix) {
        PlaceholderReplacer placeholderReplacer =
                super.createPlaceholderReplacer(enabled, placeholders, placeholderPrefix, placeholderSuffix);





        return placeholderReplacer;
    }

    @Override
    public String getDbName() {
        return "oracle";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT USER FROM DUAL");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
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
        return getMainConnection().getJdbcTemplate().queryForBoolean("SELECT CASE WHEN EXISTS(" + query + ") THEN 1 ELSE 0 END FROM DUAL", params);
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
        return new HashSet<>(getMainConnection().getJdbcTemplate()
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
        Set<String> result = new HashSet<>(Arrays.asList(
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






        result.addAll(getMainConnection().getJdbcTemplate().queryForStringList("SELECT USERNAME FROM ALL_USERS " +
                        "WHERE REGEXP_LIKE(USERNAME, '^(APEX|FLOWS)_\\d+$')" +



                                " OR ORACLE_MAINTAINED = 'Y'"



        ));
























        return result;
    }
}