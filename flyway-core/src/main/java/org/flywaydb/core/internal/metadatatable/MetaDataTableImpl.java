/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.metadatatable;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.*;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Supports reading and writing to the metadata table.
 */
public class MetaDataTableImpl implements MetaDataTable {
    private static final Log LOG = LogFactory.getLog(MetaDataTableImpl.class);

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The metadata table used by flyway.
     */
    private final Table table;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance of the metadata table support.
     *
     * @param dbSupport Database-specific functionality.
     * @param table     The metadata table used by flyway.
     */
    public MetaDataTableImpl(DbSupport dbSupport, Table table) {
        this.jdbcTemplate = dbSupport.getJdbcTemplate();
        this.dbSupport = dbSupport;
        this.table = table;
    }

    /**
     * Creates the metatable if it doesn't exist, upgrades it if it does.
     */
    private void createIfNotExists() {
        if (table.exists()) {
            return;
        }

        LOG.info("Creating Metadata table: " + table);

        String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/createMetaDataTable.sql";
        String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", table.getSchema().getName());
        placeholders.put("table", table.getName());
        String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

        SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
        sqlScript.execute(jdbcTemplate);

        LOG.debug("Metadata table " + table + " created.");
    }

    @Override
    public void lock() {
        createIfNotExists();
        table.lock();
    }

    @Override
    public void addAppliedMigration(AppliedMigration appliedMigration) {
        createIfNotExists();

        MigrationVersion version = appliedMigration.getVersion();

        try {
            int versionRank = calculateVersionRank(version);

            // Try load an updateMetaDataTable.sql file if it exists
            try {
                String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/updateMetaDataTable.sql";
                String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
                Map<String, String> placeholders = new HashMap<String, String>();

                // Placeholders for schema and table
                placeholders.put("schema", table.getSchema().getName());
                placeholders.put("table", table.getName());

                // Placeholders for column values
                placeholders.put("version_rank_val", String.valueOf(versionRank));
                placeholders.put("installed_rank_val", String.valueOf(calculateInstalledRank()));
                placeholders.put("version_val", version.toString());
                placeholders.put("description_val", appliedMigration.getDescription());
                placeholders.put("type_val", appliedMigration.getType().name());
                placeholders.put("script_val", appliedMigration.getScript());
                placeholders.put("checksum_val", String.valueOf(appliedMigration.getChecksum()));
                placeholders.put("installed_by_val", dbSupport.getCurrentUserFunction());
                placeholders.put("execution_time_val", String.valueOf(appliedMigration.getExecutionTime() * 1000L));
                placeholders.put("success_val", String.valueOf(appliedMigration.isSuccess()));

                String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

                SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);

                sqlScript.execute(jdbcTemplate);

            }
            // Fall back to hard-coded statements
            catch (FlywayException e) {
                LOG.info(e.getMessage());
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
                        appliedMigration.isSuccess()
                );
            }

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
            migrationVersions.add(MigrationVersion.fromVersion(versionStr));
        }

        Collections.sort(migrationVersions);

        for (int i = 0; i < migrationVersions.size(); i++) {
            if (version.compareTo(migrationVersions.get(i)) < 0) {
                return i + 1;
            }
        }

        return migrationVersions.size() + 1;
    }

    @Override
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
        if (!table.exists()) {
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
                    Integer checksum = rs.getInt("checksum");
                    if (rs.wasNull()) {
                        checksum = null;
                    }

                    return new AppliedMigration(
                            rs.getInt("version_rank"),
                            rs.getInt("installed_rank"),
                            MigrationVersion.fromVersion(rs.getString("version")),
                            rs.getString("description"),
                            MigrationType.valueOf(rs.getString("type")),
                            rs.getString("script"),
                            checksum,
                            rs.getTimestamp("installed_on"),
                            rs.getString("installed_by"),
                            rs.getInt("execution_time"),
                            rs.getBoolean("success")
                    );
                }
            });
        } catch (SQLException e) {
            throw new FlywayException("Error while retrieving the list of applied migrations from metadata table "
                    + table, e);
        }
    }

    @Override
    public void addBaselineMarker(final MigrationVersion baselineVersion, final String baselineDescription) {
        addAppliedMigration(new AppliedMigration(baselineVersion, baselineDescription, MigrationType.BASELINE, baselineDescription, null,
                0, true));
    }

    @Override
    public void removeFailedMigrations() {
        if (!table.exists()) {
            LOG.info("Repair of failed migration in metadata table " + table + " not necessary. No failed migration detected.");
            return;
        }

        createIfNotExists();

        try {
            int failedCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + table
                    + " WHERE " + dbSupport.quote("success") + "=" + dbSupport.getBooleanFalse());
            if (failedCount == 0) {
                LOG.info("Repair of failed migration in metadata table " + table + " not necessary. No failed migration detected.");
                return;
            }
        } catch (SQLException e) {
            throw new FlywayException("Unable to check the metadata table " + table + " for failed migrations", e);
        }

        try {
            jdbcTemplate.execute("DELETE FROM " + table
                    + " WHERE " + dbSupport.quote("success") + " = " + dbSupport.getBooleanFalse());
        } catch (SQLException e) {
            throw new FlywayException("Unable to repair metadata table " + table, e);
        }
    }

    @Override
    public void addSchemasMarker(final Schema[] schemas) {
        createIfNotExists();

        addAppliedMigration(new AppliedMigration(MigrationVersion.fromVersion("0"), "<< Flyway Schema Creation >>",
                MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true));
    }

    @Override
    public boolean hasSchemasMarker() {
        if (!table.exists()) {
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

    @Override
    public boolean hasBaselineMarker() {
        if (!table.exists()) {
            return false;
        }

        createIfNotExists();

        try {
            int count = jdbcTemplate.queryForInt(
                    "SELECT COUNT(*) FROM " + table + " WHERE " + dbSupport.quote("type") + "='INIT' OR " + dbSupport.quote("type") + "='BASELINE'");
            return count > 0;
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether the metadata table " + table + " has an baseline marker migration", e);
        }
    }

    @Override
    public AppliedMigration getBaselineMarker() {
        List<AppliedMigration> appliedMigrations = findAppliedMigrations(MigrationType.INIT);
        appliedMigrations.addAll(findAppliedMigrations(MigrationType.BASELINE));
        return appliedMigrations.isEmpty() ? null : appliedMigrations.get(0);
    }

    @Override
    public boolean hasAppliedMigrations() {
        if (!table.exists()) {
            return false;
        }

        createIfNotExists();

        try {
            int count = jdbcTemplate.queryForInt(
                    "SELECT COUNT(*) FROM " + table + " WHERE " + dbSupport.quote("type") + " NOT IN ('SCHEMA', 'INIT', 'BASELINE')");
            return count > 0;
        } catch (SQLException e) {
            throw new FlywayException("Unable to check whether the metadata table " + table + " has applied migrations", e);
        }
    }

    @Override
    public void updateChecksum(MigrationVersion version, Integer checksum) {
        LOG.info("Updating checksum of " + version + " to " + checksum + " ...");

        // Try load an updateChecksum.sql file if it exists
        try {
            String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/updateChecksum.sql";
            String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
            Map<String, String> placeholders = new HashMap<String, String>();

            // Placeholders for column names
            placeholders.put("schema", table.getSchema().getName());
            placeholders.put("table", table.getName());

            // Placeholders for column values
            placeholders.put("version_val", version.toString());
            placeholders.put("checksum_val", String.valueOf(checksum));

            String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

            SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);

            sqlScript.execute(jdbcTemplate);

        } catch (FlywayException fe) {
            try {
                jdbcTemplate.update("UPDATE " + table + " SET " + dbSupport.quote("checksum") + "=" + checksum
                        + " WHERE " + dbSupport.quote("version") + "='" + version + "'");
            }
            catch (SQLException e) {
                throw new FlywayException("Unable to update checksum in metadata table " + table
                        + " for version " + version + " to " + checksum, e);
            }
        }
    }

    @Override
    public String toString() {
        return table.toString();
    }
}
