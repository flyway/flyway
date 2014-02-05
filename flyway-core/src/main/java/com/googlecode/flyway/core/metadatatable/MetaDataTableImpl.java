/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.metadatatable;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.resolver.MigrationResolver;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.jdbc.RowMapper;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supports reading and writing to the metadata table.
 */
public class MetaDataTableImpl implements MetaDataTable {
    private static final Log LOG = LogFactory.getLog(MetaDataTableImpl.class);

    /**
     * Flag indicating whether the upgrade has already been executed.
     */
    private boolean upgraded;

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The metadata table used by flyway.
     */
    private final Table table;

    /**
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance of the metadata table support.
     *
     * @param dbSupport         Database-specific functionality.
     * @param table             The metadata table used by flyway.
     * @param migrationResolver For resolving available migrations.
     */
    public MetaDataTableImpl(DbSupport dbSupport, Table table, MigrationResolver migrationResolver) {
        this.jdbcTemplate = dbSupport.getJdbcTemplate();
        this.dbSupport = dbSupport;
        this.table = table;
        this.migrationResolver = migrationResolver;
    }

    /**
     * Creates the metatable if it doesn't exist, upgrades it if it does.
     */
    private void createIfNotExists() {
        if (table.existsNoQuotes() || table.exists()) {
            if (!upgraded) {
                new MetaDataTableTo20FormatUpgrader(dbSupport, table, migrationResolver).upgrade();
                new MetaDataTableTo202FormatUpgrader(dbSupport, table).upgrade();
                new MetaDataTableTo21FormatUpgrader(dbSupport, table).upgrade();
                upgraded = true;
            }
            return;
        }

        LOG.info("Creating Metadata table: " + table);

        final String source =
                new ClassPathResource(dbSupport.getScriptLocation() + "createMetaDataTable.sql").loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", table.getSchema().getName());
        placeholders.put("table", table.getName());
        final String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}", true).replacePlaceholders(source);

        SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
        sqlScript.execute(jdbcTemplate);

        LOG.debug("Metadata table " + table + " created.");
    }

    public void lock() {
        createIfNotExists();
        table.lock();
    }

    public void addAppliedMigration(AppliedMigration appliedMigration) {
        createIfNotExists();

        MigrationVersion version = appliedMigration.getVersion();
        try {
            int versionRank = calculateVersionRank(version);

            jdbcTemplate.update("UPDATE " + table
                    + " SET " + dbSupport.quote("version_rank") + " = " + dbSupport.quote("version_rank")
                    + " + 1 WHERE " + dbSupport.quote("version_rank") + " >= ?", versionRank);
            jdbcTemplate.update("INSERT INTO " + table
                    + " (" + dbSupport.quote("version_rank")
                    + "," + dbSupport.quote("installed_rank")
                    + "," + dbSupport.quote("version")
                    + "," + dbSupport.quote("description")
                    + "," + dbSupport.quote("type")
                    + "," + dbSupport.quote("script")
                    + "," + dbSupport.quote("checksum")
                    + "," + dbSupport.quote("installed_by")
                    + "," + dbSupport.quote("execution_time")
                    + "," + dbSupport.quote("success")
                    + ")"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, " + dbSupport.getCurrentUserFunction() + ", ?, ?)",
                    versionRank,
                    calculateInstalledRank(),
                    version.toString(),
                    appliedMigration.getDescription(),
                    appliedMigration.getType().name(),
                    appliedMigration.getScript(),
                    appliedMigration.getChecksum(),
                    appliedMigration.getExecutionTime(),
                    appliedMigration.isSuccess());
            LOG.debug("MetaData table " + table + " successfully updated to reflect changes");
        } catch (SQLException e) {
            throw new FlywayException("Unable to insert row for version '" + version + "' in metadata table " + table, e);
        }
    }

    /**
     * Calculates the installed rank for the new migration to be inserted.
     *
     * @return The installed rank.
     */
    private int calculateInstalledRank() throws SQLException {
        int currentMax = jdbcTemplate.queryForInt("SELECT MAX(" + dbSupport.quote("installed_rank") + ")"
                + " FROM " + table);
        return currentMax + 1;
    }

    /**
     * Calculate the rank for this new version about to be inserted.
     *
     * @param version The version to calculated for.
     * @return The rank.
     */
    private int calculateVersionRank(MigrationVersion version) throws SQLException {
        List<String> versions = jdbcTemplate.queryForStringList("select " + dbSupport.quote("version") + " from " + table);

        List<MigrationVersion> migrationVersions = new ArrayList<MigrationVersion>();
        for (String versionStr : versions) {
            migrationVersions.add(new MigrationVersion(versionStr));
        }

        Collections.sort(migrationVersions);

        for (int i = 0; i < migrationVersions.size(); i++) {
            if (version.compareTo(migrationVersions.get(i)) < 0) {
                return i + 1;
            }
        }

        return migrationVersions.size() + 1;
    }

    public List<AppliedMigration> allAppliedMigrations() {
        return findAppliedMigrations();
    }

    /**
     * Retrieve the applied migrations from the metadata table.
     *
     * @param migrationTypes The specific migration types to look for. (Optional) None means find all migrations.
     * @return The applied migrations.
     */
    private List<AppliedMigration> findAppliedMigrations(MigrationType... migrationTypes) {
        if (!table.existsNoQuotes() && !table.exists()) {
            return new ArrayList<AppliedMigration>();
        }

        createIfNotExists();

        String query = "SELECT " + dbSupport.quote("version_rank")
                + "," + dbSupport.quote("installed_rank")
                + "," + dbSupport.quote("version")
                + "," + dbSupport.quote("description")
                + "," + dbSupport.quote("type")
                + "," + dbSupport.quote("script")
                + "," + dbSupport.quote("checksum")
                + "," + dbSupport.quote("installed_on")
                + "," + dbSupport.quote("installed_by")
                + "," + dbSupport.quote("execution_time")
                + "," + dbSupport.quote("success")
                + " FROM " + table;

        if (migrationTypes.length > 0) {
            query += " WHERE " + dbSupport.quote("type") + " IN (";
            for (int i = 0; i < migrationTypes.length; i++) {
                if (i > 0) {
                    query += ",";
                }
                query += "'" + migrationTypes[i] + "'";
            }
            query += ")";
        }

        query += " ORDER BY " + dbSupport.quote("version_rank");

        try {
            return jdbcTemplate.query(query, new RowMapper<AppliedMigration>() {
                public AppliedMigration mapRow(final ResultSet rs) throws SQLException {
                    return new AppliedMigration(
                            rs.getInt("version_rank"),
                            rs.getInt("installed_rank"),
                            new MigrationVersion(rs.getString("version")),
                            rs.getString("description"),
                            MigrationType.valueOf(rs.getString("type")),
                            rs.getString("script"),
                            toInteger((Number) rs.getObject("checksum")),
                            rs.getTimestamp("installed_on"),
                            rs.getString("installed_by"),
                            toInteger((Number) rs.getObject("execution_time")),
                            rs.getBoolean("success")
                    );
                }
            });
        } catch (SQLException e) {
            throw new FlywayException("Error while retrieving the list of applied migrations from metadata table "
                    + table, e);
        }
    }

    /**
     * Converts this number into an Integer.
     *
     * @param number The Number to convert.
     * @return The matching Integer.
     */
    private Integer toInteger(Number number) {
        if (number == null) {
            return null;
        }

        return number.intValue();
    }

    public void addInitMarker(final MigrationVersion initVersion, final String initDescription) {
        addAppliedMigration(new AppliedMigration(initVersion, initDescription, MigrationType.INIT, initDescription, null,
                0, true));
    }

    public void repair() {
        if (!table.existsNoQuotes() && !table.exists()) {
            LOG.info("Repair of metadata table " + table + " not necessary. No failed migration detected.");
            return;
        }

        createIfNotExists();

        try {
            int failedCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + table
                    + " WHERE " + dbSupport.quote("success") + "=" + dbSupport.getBooleanFalse());
            if (failedCount == 0) {
                LOG.info("Repair of metadata table " + table + " not necessary. No failed migration detected.");
                return;
            }
        } catch (SQLException e) {
            throw new FlywayException("Unable to check the metadata table " + table + " for failed migrations", e);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            jdbcTemplate.execute("DELETE FROM " + table
                    + " WHERE " + dbSupport.quote("success") + " = " + dbSupport.getBooleanFalse());
        } catch (SQLException e) {
            throw new FlywayException("Unable to repair metadata table " + table, e);
        }

        stopWatch.stop();

        LOG.info("Metadata table " + table + " successfully repaired (execution time "
                + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
        LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
    }

    public void addSchemasMarker(final Schema[] schemas) {
        createIfNotExists();

        addAppliedMigration(new AppliedMigration(new MigrationVersion("0"), "<< Flyway Schema Creation >>",
                MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true));
    }

    public boolean hasSchemasMarker() {
        if (!table.existsNoQuotes() && !table.exists()) {
            return false;
        }

        createIfNotExists();

        try {
            int count = jdbcTemplate.queryForInt(
                    "SELECT COUNT(*) FROM " + table + " WHERE " + dbSupport.quote("type") + "='SCHEMA'");
            return count > 0;
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether the metadata table " + table + " has a schema marker migration", e);
        }
    }

    public boolean hasInitMarker() {
        if (!table.existsNoQuotes() && !table.exists()) {
            return false;
        }

        createIfNotExists();

        try {
            int count = jdbcTemplate.queryForInt(
                    "SELECT COUNT(*) FROM " + table + " WHERE " + dbSupport.quote("type") + "='INIT'");
            return count > 0;
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether the metadata table " + table + " has an init marker migration", e);
        }
    }

    public AppliedMigration getInitMarker() {
        List<AppliedMigration> appliedMigrations = findAppliedMigrations(MigrationType.INIT);
        return appliedMigrations.isEmpty() ? null : appliedMigrations.get(0);
    }

    public boolean hasAppliedMigrations() {
        if (!table.existsNoQuotes() && !table.exists()) {
            return false;
        }

        createIfNotExists();

        try {
            int count = jdbcTemplate.queryForInt(
                    "SELECT COUNT(*) FROM " + table + " WHERE " + dbSupport.quote("type") + " NOT IN ('SCHEMA', 'INIT')");
            return count > 0;
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether the metadata table " + table + " has applied migrations", e);
        }
    }

    @Override
    public String toString() {
        return table.toString();
    }
}
