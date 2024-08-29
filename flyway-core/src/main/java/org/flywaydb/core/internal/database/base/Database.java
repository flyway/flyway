/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.database.base;

import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.AbbreviationUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.Closeable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_AWS_VM;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_AZURE_URL_IDENTIFIER;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_AZURE_VM;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_GCP_URL_IDENTIFIER;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_GCP_VM;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_LOCAL;
import static org.flywaydb.core.internal.database.base.DatabaseConstants.DATABASE_HOSTING_RDS_URL_IDENTIFIER;
import static org.flywaydb.core.internal.util.FlywayDbWebsiteLinks.COMMUNITY_SUPPORT;

/**
 * Abstraction for database-specific functionality.
 */
@CustomLog
public abstract class Database<C extends Connection> implements Closeable {
    protected final DatabaseType databaseType;
    @Getter
    protected final Configuration configuration;
    protected final StatementInterceptor statementInterceptor;
    protected final JdbcConnectionFactory jdbcConnectionFactory;
    protected final DatabaseMetaData jdbcMetaData;
    /**
     * The main JDBC connection, without any wrapping.
     */
    protected final java.sql.Connection rawMainJdbcConnection;
    protected JdbcTemplate jdbcTemplate;
    private C migrationConnection;
    private C eventConnection;
    private C mainConnection;
    /**
     * The 'major.minor' version of this database.
     */
    private MigrationVersion version;
    /**
     * The user who applied the migrations.
     */
    private String installedBy;

    public Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        this.databaseType = jdbcConnectionFactory.getDatabaseType();
        this.configuration = configuration;
        this.rawMainJdbcConnection = jdbcConnectionFactory.openConnection();
        try {
            this.jdbcMetaData = rawMainJdbcConnection.getMetaData();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to get metadata for connection", e);
        }
        this.jdbcTemplate = new JdbcTemplate(rawMainJdbcConnection, databaseType);
        this.jdbcConnectionFactory = jdbcConnectionFactory;
        this.statementInterceptor = statementInterceptor;
    }

    /**
     * Retrieves a Flyway Connection for this JDBC connection.
     */
    private C getConnection(java.sql.Connection connection) {
        return doGetConnection(connection);
    }

    /**
     * Retrieves a Flyway Connection for this JDBC connection.
     */
    protected abstract C doGetConnection(java.sql.Connection connection);

    /**
     * Ensure Flyway supports this version of this database.
     */
    public abstract void ensureSupported(Configuration configuration);

    /**
     * @return The 'major.minor' version of this database.
     */
    public final MigrationVersion getVersion() {
        if (version == null) {
            version = determineVersion();
        }
        return version;
    }

    protected final void ensureDatabaseIsRecentEnough(String oldestSupportedVersion) {
        if (!getVersion().isAtLeast(oldestSupportedVersion)) {
            throw new FlywayDbUpgradeRequiredException(
                    databaseType,
                    computeVersionDisplayName(getVersion()),
                    computeVersionDisplayName(MigrationVersion.fromVersion(oldestSupportedVersion)));
        }
    }

    /**
     * Ensure this database it at least as recent as this version otherwise suggest upgrade to this higher edition of
     * Flyway.
     */
    protected final void ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition(String oldestSupportedVersionInThisEdition,
                                                                                            List<Tier> editionWhereStillSupported, Configuration configuration) {
        if (!LicenseGuard.isLicensed(configuration, editionWhereStillSupported) &&
                !getVersion().isAtLeast(oldestSupportedVersionInThisEdition)) {
            LOG.info(getDatabaseType().getName() + " " + computeVersionDisplayName(getVersion()) + " is outside of Redgate community support. See " + COMMUNITY_SUPPORT + " for details");
        }

    }

    protected final void recommendFlywayUpgradeIfNecessary(String newestSupportedVersion) {
        if (getVersion().isNewerThan(newestSupportedVersion)) {
            recommendFlywayUpgrade(newestSupportedVersion);
        }
    }

    protected final void recommendFlywayUpgradeIfNecessaryForMajorVersion(String newestSupportedVersion) {
        if (getVersion().isMajorNewerThan(newestSupportedVersion)) {
            recommendFlywayUpgrade(newestSupportedVersion);
        }
    }

    protected final void notifyDatabaseIsNotFormallySupported() {
        LOG.warn("Support for " + databaseType + " is provided only on a community-led basis, and is not formally supported by Redgate");
    }

    private void recommendFlywayUpgrade(String newestSupportedVersion) {
        String message = "Flyway upgrade recommended: " + databaseType + " " + computeVersionDisplayName(getVersion())
                + " is newer than this version of Flyway and support has not been tested."
                + " The latest supported version of " + databaseType + " is " + newestSupportedVersion + ".";
        LOG.warn(message);
    }

    /**
     * Compute the user-friendly display name for this database version.
     */
    protected String computeVersionDisplayName(MigrationVersion version) {
        return version.getVersion();
    }

    public Delimiter getDefaultDelimiter() {
        return Delimiter.SEMICOLON;
    }

    /**
     * @return The name of the database, by default as determined by JDBC.
     */
    public final String getCatalog() {
        try {
            return doGetCatalog();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error retrieving the database name", e);
        }
    }

    protected String doGetCatalog() throws SQLException {
        return getMainConnection().getJdbcConnection().getCatalog();
    }

    public final String getCurrentUser() {
        try {
            return doGetCurrentUser();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error retrieving the database user", e);
        }
    }

    protected String doGetCurrentUser() throws SQLException {
        return jdbcMetaData.getUserName();
    }

    public abstract boolean supportsDdlTransactions();

    /**
     * @return The representation of the value {@code true} in a boolean column.
     */
    public abstract String getBooleanTrue();

    /**
     * @return The representation of the value {@code false} in a boolean column.
     */
    public abstract String getBooleanFalse();

    /**
     * Quotes these identifiers for use in SQL queries. Multiple identifiers will be quoted and separated by a dot.
     */
    public final String quote(String... identifiers) {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (String identifier : identifiers) {
            if (!first) {
                result.append(".");
            }
            first = false;
            result.append(doQuote(identifier));
        }

        return result.toString();
    }

    /**
     * Quotes this identifier for use in SQL queries.
     */
    public String doQuote(String identifier) {
        return getOpenQuote() + identifier + getCloseQuote();
    }

    protected String getOpenQuote() {
        return "\"";
    }

    protected String getCloseQuote() {
        return "\"";
    }

    protected String getEscapedQuote() {
        return "";
    }

    public String unQuote(String identifier) {
        String open = getOpenQuote();
        String close = getCloseQuote();

        if (!open.equals("") && !close.equals("") && identifier.startsWith(open) && identifier.endsWith(close)) {
            identifier = identifier.substring(open.length(), identifier.length() - close.length());
            if (!getEscapedQuote().equals("")) {
                identifier = StringUtils.replaceAll(identifier, getEscapedQuote(), close);
            }
        }

        return identifier;
    }

    /**
     * @return {@code true} if this database uses a catalog to represent a schema, or {@code false} if a schema is
     * simply a schema.
     */
    public abstract boolean catalogIsSchema();

    /**
     * @return Whether to use a single connection for both schema history table management and applying migrations.
     */
    public boolean useSingleConnection() {
        return false;
    }

    public DatabaseMetaData getJdbcMetaData() {
        return jdbcMetaData;
    }

    /**
     * @return The main connection used to manipulate the schema history.
     */
    public final C getMainConnection() {
        if (mainConnection == null) {
            this.mainConnection = getConnection(rawMainJdbcConnection);
        }
        return mainConnection;
    }

    /**
     * @return The migration connection used to apply migrations.
     */
    public final C getMigrationConnection() {
        if (migrationConnection == null) {
            if (useSingleConnection()) {
                this.migrationConnection = getMainConnection();
            } else {
                this.migrationConnection = getConnection(jdbcConnectionFactory.openConnection());
            }
        }
        return migrationConnection;
    }

    /**
     * @return The event connection used to handle event callbacks.
     * The reason for creating an event connection is that if using the migration connection instead, it may trigger an unwanted commit which breaks
     * any ongoing migration transaction.
     */
    public final C getEventConnection() {
        if (!hasEventConnection()) {
            eventConnection = getConnection(jdbcConnectionFactory.openConnection());
        }
        return eventConnection;
    }

    public final boolean hasEventConnection(){
        return eventConnection != null;
    }

    /**
     * An event connection should be disposed after usage to minimize long-standing connections.
     */
    public void disposeEventConnection() {
        if (hasEventConnection()) {
            eventConnection.close();
            eventConnection = null;
        }
    }

    /**
     * @return The major and minor version of the database.
     */
    protected MigrationVersion determineVersion() {
        try {
            return MigrationVersion.fromVersion(jdbcMetaData.getDatabaseMajorVersion() + "." + jdbcMetaData.getDatabaseMinorVersion());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the major version of the database", e);
        }
    }

    /**
     * Retrieves the script used to create the schema history table.
     *
     * @param sqlScriptFactory The factory used to create the SQL script.
     * @param table The table to create.
     * @param baseline Whether to include the creation of a baseline marker.
     */
    public final SqlScript getCreateScript(SqlScriptFactory sqlScriptFactory, Table table, boolean baseline) {
        return sqlScriptFactory.createSqlScript(new StringResource(getRawCreateScript(table, baseline)), false, null);
    }

    public abstract String getRawCreateScript(Table table, boolean baseline);

    public String getInsertStatement(Table table) {
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    public String getUpdateStatement(Table table) {
        return "UPDATE " + table
                + " SET "
                + quote("description") + "=? , "
                + quote("type") + "=? , "
                + quote("checksum") + "=?"
                + " WHERE " + quote("installed_rank") + "=?";
    }

    protected String getBaselineStatement(Table table) {
        return String.format(getInsertStatement(table).replace("?", "%s"),
                             1,
                             "'" + configuration.getBaselineVersion() + "'",
                             "'" + AbbreviationUtils.abbreviateDescription(configuration.getBaselineDescription()) + "'",
                             "'" + CoreMigrationType.BASELINE + "'",
                             "'" + AbbreviationUtils.abbreviateScript(configuration.getBaselineDescription()) + "'",
                             "NULL",
                             "'" + getInstalledBy() + "'",
                             0,
                             getBooleanTrue()
                            );
    }

    public String getSelectStatement(Table table) {
        return "SELECT " + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_on")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + " FROM " + table
                + " WHERE " + quote("installed_rank") + " > ?"
                + " ORDER BY " + quote("installed_rank");
    }

    public Pair<String, Object> getDeleteStatement(Table table, boolean version, String filter) {
        String deleteStatement = "DELETE FROM " + table +
            " WHERE " + quote("success") + " = " + getBooleanFalse() + " AND " +
            (version ?
                quote("version") + " = ?" :
                quote("description") + " = ?");

        return Pair.of(deleteStatement, filter);
    }

    public final String getInstalledBy() {
        if (installedBy == null) {
            installedBy = configuration.getInstalledBy() == null ? getCurrentUser() : configuration.getInstalledBy();
        }
        return installedBy;
    }

    public void close() {
        if (!useSingleConnection() && migrationConnection != null) {
            migrationConnection.close();
        }
        if (mainConnection != null) {
            mainConnection.close();
        }

        disposeEventConnection();

        if (rawMainJdbcConnection != null) {
            JdbcUtils.closeConnection(rawMainJdbcConnection);
        }
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public boolean supportsEmptyMigrationDescription() {
        return true;
    }

    public boolean supportsMultiStatementTransactions() {
        return true;
    }

    /**
     * Cleans all the objects in this database that need to be cleaned before each schema.
     */
    public void cleanPreSchemas() {
        try {
            doCleanPreSchemas();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean database " + this, e);
        }
    }

    /**
     * Cleans all the objects in this database that need to be cleaned before each schema.
     *
     * @throws SQLException when the clean failed.
     */
    protected void doCleanPreSchemas() throws SQLException {}

    /**
     * Cleans all the objects in this database that need to be cleaned after each schema.
     *
     * @param schemas The list of schemas managed by Flyway.
     */
    public void cleanPostSchemas(Schema[] schemas) {
        try {
            doCleanPostSchemas(schemas);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean schema " + this, e);
        }
    }

    /**
     * Cleans all the objects in this database that need to be cleaned after each schema.
     *
     * @param schemas The list of schemas managed by Flyway.
     * @throws SQLException when the clean failed.
     */
    protected void doCleanPostSchemas(Schema[] schemas) throws SQLException {}

    public Schema[] getAllSchemas() {
        throw new UnsupportedOperationException("Getting all schemas not supported for " + getDatabaseType().getName());
    }

    public String getDatabaseHosting() {
        String url = configuration.getUrl();

        if (DATABASE_HOSTING_AZURE_URL_IDENTIFIER.matcher(url).find()) {
            return DATABASE_HOSTING_AZURE_VM;
        } else if (DATABASE_HOSTING_RDS_URL_IDENTIFIER.matcher(url).find()) {
            return DATABASE_HOSTING_AWS_VM;
        } else if (url.contains(DATABASE_HOSTING_GCP_URL_IDENTIFIER)) {
            return DATABASE_HOSTING_GCP_VM;
        }

        return DATABASE_HOSTING_LOCAL;
    }
}
