/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.AbbreviationUtils;

import java.io.Closeable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Abstraction for database-specific functionality.
 */
public abstract class Database<C extends Connection> implements Closeable {
    private static final Log LOG = LogFactory.getLog(Database.class);

    /**
     * The type of database this is.
     */
    protected final DatabaseType databaseType;

    /**
     * The Flyway configuration.
     */
    protected final Configuration configuration;

    /**
     * The JDBC metadata to use.
     */
    protected final DatabaseMetaData jdbcMetaData;

    /**
     * The main JDBC connection, without any wrapping.
     */
    protected final java.sql.Connection rawMainJdbcConnection;

    /**
     * The main connection to use.
     */
    private C mainConnection;

    /**
     * The connection to use for migrations.
     */
    private C migrationConnection;

    protected final JdbcConnectionFactory jdbcConnectionFactory;





    /**
     * The major.minor version of the database.
     */
    private MigrationVersion version;

    /**
     * The user who applied the migrations.
     */
    private String installedBy;

    protected JdbcTemplate jdbcTemplate;

    /**
     * Creates a new Database instance with this JdbcTemplate.
     *
     * @param configuration The Flyway configuration.
     */
    public Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
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



    }

    /**
     * Retrieves a Flyway Connection for this JDBC connection.
     *
     * @param connection The JDBC connection to wrap.
     * @return The Flyway Connection.
     */
    private C getConnection(java.sql.Connection connection) {
        return doGetConnection(connection);
    }

    /**
     * Retrieves a Flyway Connection for this JDBC connection.
     *
     * @param connection The JDBC connection to wrap.
     * @return The Flyway Connection.
     */
    protected abstract C doGetConnection(java.sql.Connection connection);

    /**
     * Ensures Flyway supports this version of this database.
     */
    public abstract void ensureSupported();

    /**
     * @return The major.minor version of the database.
     */
    public final MigrationVersion getVersion() {
        if (version == null) {
            version = determineVersion();
        }
        return version;
    }

    protected final void ensureDatabaseIsRecentEnough(String oldestSupportedVersion) {
        if (!getVersion().isAtLeast(oldestSupportedVersion)) {
            throw new FlywayDbUpgradeRequiredException(databaseType, computeVersionDisplayName(getVersion()),
                    computeVersionDisplayName(MigrationVersion.fromVersion(oldestSupportedVersion)));
        }
    }

    /**
     * Ensures this database it at least at recent as this version otherwise suggest upgrade to this higher edition of
     * Flyway.
     *
     * @param oldestSupportedVersionInThisEdition The oldest supported version of the database by this edition of Flyway.
     * @param editionWhereStillSupported          The edition of Flyway that still supports this version of the database,
     *                                            in case it's too old.
     */
    protected final void ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition(String oldestSupportedVersionInThisEdition,
                                                                                            Edition editionWhereStillSupported) {
        if (!getVersion().isAtLeast(oldestSupportedVersionInThisEdition)) {
            throw new FlywayEditionUpgradeRequiredException(
                    editionWhereStillSupported,
                    databaseType,
                    computeVersionDisplayName(getVersion()));
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

    private void recommendFlywayUpgrade(String newestSupportedVersion) {
        String message = "Flyway upgrade recommended: " + databaseType + " " + computeVersionDisplayName(getVersion())
                + " is newer than this version of Flyway and support has not been tested. "
                + "The latest supported version of " + databaseType + " is " + newestSupportedVersion + ".";

        LOG.warn(message);
    }

    /**
     * Compute the user-friendly display name for this database version.
     *
     * @return The user-friendly display name.
     */
    protected String computeVersionDisplayName(MigrationVersion version) {
        return version.getVersion();
    }

    /**
     * @return The default delimiter for this database.
     */
    public Delimiter getDefaultDelimiter() {
        return Delimiter.SEMICOLON;
    }

    /**
     * @return The current database user.
     */
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

    /**
     * Checks whether DDL transactions are supported by this database.
     *
     * @return {@code true} if DDL transactions are supported, {@code false} if not.
     */
    public abstract boolean supportsDdlTransactions();

    /**
     * Whether to add the baseline marker directly as part of the create table statement for this database.
     */
    public boolean useDirectBaseline() {
        return false;
    }

    /**
     * @return {@code true} if this database supports changing a connection's current schema. {@code false if not}.
     */
    public abstract boolean supportsChangingCurrentSchema();













    /**
     * @return The representation of the value {@code true} in a boolean column.
     */
    public abstract String getBooleanTrue();

    /**
     * @return The representation of the value {@code false} in a boolean column.
     */
    public abstract String getBooleanFalse();

    /**
     * Quote these identifiers for use in sql queries. Multiple identifiers will be quoted and separated by a dot.
     *
     * @param identifiers The identifiers to quote.
     * @return The fully qualified quoted identifiers.
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
     * Quote this identifier for use in sql queries.
     *
     * @param identifier The identifier to quote.
     * @return The fully qualified quoted identifier.
     */
    protected abstract String doQuote(String identifier);

    /**
     * @return {@code true} if this database use a catalog to represent a schema. {@code false} if a schema is simply a schema.
     */
    public abstract boolean catalogIsSchema();

    /**
     * @return Whether to only use a single connection for both schema history table management and applying migrations.
     */
    public boolean useSingleConnection() {
        return false;
    }

    public DatabaseMetaData getJdbcMetaData() {
        return jdbcMetaData;
    }

    /**
     * @return The main connection, used to manipulate the schema history.
     */
    public final C getMainConnection() {
        if (mainConnection == null) {
            this.mainConnection = getConnection(rawMainJdbcConnection);
        }
        return mainConnection;
    }

    /**
     * @return The migration connection, used to apply migrations.
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
     * @param table    The table to create.
     * @param baseline Whether to include the creation of a baseline marker.
     * @return The script.
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

    public final String getBaselineStatement(Table table) {
        return String.format(getInsertStatement(table).replace("?", "%s"),
                1,
                "'" + configuration.getBaselineVersion() + "'",
                "'" + AbbreviationUtils.abbreviateDescription(configuration.getBaselineDescription()) + "'",
                "'" + MigrationType.BASELINE + "'",
                "'" + AbbreviationUtils.abbreviateScript(configuration.getBaselineDescription()) + "'",
                "NULL",
                "'" + installedBy + "'",
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
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     *  Whether the database supports an empty string as a migration description.
     */
    public boolean supportsEmptyMigrationDescription() { return true; }

    /**
     * Whether the database supports multi-statement transactions
     */
    public boolean supportsMultiStatementTransactions() { return true; }

    /**
     * Cleans all the objects in this database that need to be done prior to cleaning schemas.
     */
    public void cleanPreSchemas() {
        try {
            doCleanPreSchemas();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean database " + this, e);
        }
    }

    /**
     * Cleans all the objects in this database that need to be done prior to cleaning schemas.
     *
     * @throws SQLException when the clean failed.
     */
    protected void doCleanPreSchemas() throws SQLException {
        // Default is to do nothing.
    }

    /**
     * Cleans all the objects in this database that need to be done after cleaning schemas.
     */
    public void cleanPostSchemas() {
        try {
            doCleanPostSchemas();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean schema " + this, e);
        }
    }

    /**
     * Cleans all the objects in this database that need to be done after cleaning schemas.
     *
     * @throws SQLException when the clean failed.
     */
    protected void doCleanPostSchemas() throws SQLException {
        // Default is to do nothing
    }
}