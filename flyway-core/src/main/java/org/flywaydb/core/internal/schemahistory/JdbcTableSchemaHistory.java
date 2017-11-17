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
package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Supports reading and writing to the metadata table.
 */
public class JdbcTableSchemaHistory extends SchemaHistory {
    private static final Log LOG = LogFactory.getLog(JdbcTableSchemaHistory.class);

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
     * Applied migration cache.
     */
    private final LinkedList<AppliedMigration> cache = new LinkedList<AppliedMigration>();

    /**
     * The current user in the database.
     */
    private String installedBy;

    /**
     * Creates a new instance of the metadata table support.
     *
     * @param dbSupport   Database-specific functionality.
     * @param table       The metadata table used by flyway.
     * @param installedBy The current user in the database.
     */
    public JdbcTableSchemaHistory(DbSupport dbSupport, Table table, String installedBy) {
        this.jdbcTemplate = dbSupport.getJdbcTemplate();
        this.dbSupport = dbSupport;
        this.table = table;
        this.installedBy = installedBy;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public boolean exists() {
        return table.exists();
    }

    /**
     * Creates the metatable if it doesn't exist, upgrades it if it does.
     */
    private void createIfNotExists() {
        int retries = 0;
        while (!table.exists()) {
            if (retries == 0) {
                LOG.info("Creating Metadata table: " + table);
            }

            try {
                String source = dbSupport.getCreateScript();

                Map<String, String> placeholders = new HashMap<String, String>();
                placeholders.put("schema", table.getSchema().getName());
                placeholders.put("table", table.getName());
                String sourceNoPlaceholders =
                        new PlaceholderReplacer(placeholders, "${", "}")
                                .replacePlaceholders(source);

                final SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
                sqlScript.execute(jdbcTemplate);

                LOG.debug("Metadata table " + table + " created.");
            } catch (FlywayException e) {
                if (++retries >= 10) {
                    throw e;
                }
                try {
                    LOG.debug("Metadata table creation failed. Retrying in 1 sec ...");
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public <T> T lock(Callable<T> callable) {
        createIfNotExists();
        return dbSupport.lock(table, callable);
    }

    @Override
    protected void doAddAppliedMigration(MigrationVersion version, String description, MigrationType type, String script, Integer checksum, int executionTime, boolean success) {
        dbSupport.changeCurrentSchemaTo(table.getSchema());

        createIfNotExists();

        // Lock again for databases with no DDL transactions to prevent implicit commits from triggering deadlocks
        // in highly concurrent environments
        table.lock();

        try {
            String versionStr = version == null ? null : version.toString();
            int installedRank = type == MigrationType.SCHEMA ? 0 : calculateInstalledRank();

            // Try load an updateMetaDataTable.sql file if it exists
            String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/updateMetaDataTable.sql";
            ClassPathResource classPathResource = new ClassPathResource(resourceName, getClass().getClassLoader());
            if (classPathResource.exists()) {
                String source = classPathResource.loadAsString("UTF-8");
                Map<String, String> placeholders = new HashMap<String, String>();

                // Placeholders for schema and table
                placeholders.put("schema", table.getSchema().getName());
                placeholders.put("table", table.getName());

                // Placeholders for column values
                placeholders.put("installed_rank_val", String.valueOf(installedRank));
                placeholders.put("version_val", versionStr);
                placeholders.put("description_val", description);
                placeholders.put("type_val", type.name());
                placeholders.put("script_val", script);
                placeholders.put("checksum_val", String.valueOf(checksum));
                placeholders.put("installed_by_val", installedBy);
                placeholders.put("execution_time_val", String.valueOf(executionTime * 1000L));
                placeholders.put("success_val", String.valueOf(success));

                String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

                SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);

                sqlScript.execute(jdbcTemplate);
            } else {
                // Fall back to hard-coded statements
                jdbcTemplate.update("INSERT INTO " + table
                                + " (" + dbSupport.quote("installed_rank")
                                + "," + dbSupport.quote("version")
                                + "," + dbSupport.quote("description")
                                + "," + dbSupport.quote("type")
                                + "," + dbSupport.quote("script")
                                + "," + dbSupport.quote("checksum")
                                + "," + dbSupport.quote("installed_by")
                                + "," + dbSupport.quote("execution_time")
                                + "," + dbSupport.quote("success")
                                + ")"
                                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        installedRank, versionStr, description, type.name(), script, checksum, installedBy,
                        executionTime, success);
            }

            LOG.debug("MetaData table " + table + " successfully updated to reflect changes");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to insert row for version '" + version + "' in metadata table " + table, e);
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

        int minInstalledRank = cache.isEmpty() ? -1 : cache.getLast().getInstalledRank();

        String query = "SELECT " + dbSupport.quote("installed_rank")
                + "," + dbSupport.quote("version")
                + "," + dbSupport.quote("description")
                + "," + dbSupport.quote("type")
                + "," + dbSupport.quote("script")
                + "," + dbSupport.quote("checksum")
                + "," + dbSupport.quote("installed_on")
                + "," + dbSupport.quote("installed_by")
                + "," + dbSupport.quote("execution_time")
                + "," + dbSupport.quote("success")
                + " FROM " + table
                + " WHERE " + dbSupport.quote("installed_rank") + " > " + minInstalledRank;

        if (migrationTypes.length > 0) {
            query += " AND " + dbSupport.quote("type") + " IN (";
            StringBuilder queryBuilder = new StringBuilder(query);
            for (int i = 0; i < migrationTypes.length; i++) {
                if (i > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append("'").append(migrationTypes[i]).append("'");
            }
            query = queryBuilder.toString();
            query += ")";
        }

        query += " ORDER BY " + dbSupport.quote("installed_rank");

        try {
            cache.addAll(jdbcTemplate.query(query, new RowMapper<AppliedMigration>() {
                public AppliedMigration mapRow(final ResultSet rs) throws SQLException {
                    Integer checksum = rs.getInt("checksum");
                    if (rs.wasNull()) {
                        checksum = null;
                    }

                    return new AppliedMigration(
                            rs.getInt("installed_rank"),
                            rs.getString("version") != null ? MigrationVersion.fromVersion(rs.getString("version")) : null,
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
            }));
            return cache;
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while retrieving the list of applied migrations from metadata table "
                    + table, e);
        }
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
            throw new FlywaySqlException("Unable to check the metadata table " + table + " for failed migrations", e);
        }

        try {
            jdbcTemplate.execute("DELETE FROM " + table
                    + " WHERE " + dbSupport.quote("success") + " = " + dbSupport.getBooleanFalse());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair metadata table " + table, e);
        }
    }

    @Override
    public void addSchemasMarker(final Schema[] schemas) {
        createIfNotExists();

        // Lock again for databases with no DDL transactions to prevent implicit commits from triggering deadlocks
        // in highly concurrent environments
        table.lock();

        doAddSchemasMarker(schemas);
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
            throw new FlywaySqlException("Unable to check whether the metadata table " + table + " has a schema marker migration", e);
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
            throw new FlywaySqlException("Unable to check whether the metadata table " + table + " has an baseline marker migration", e);
        }
    }

    @Override
    public AppliedMigration getBaselineMarker() {
        List<AppliedMigration> appliedMigrations = findAppliedMigrations(MigrationType.BASELINE);
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
            throw new FlywaySqlException("Unable to check whether the metadata table " + table + " has applied migrations", e);
        }
    }

    @Override
    public void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration) {
        clearCache();

        MigrationVersion version = appliedMigration.getVersion();

        String description = resolvedMigration.getDescription();
        Integer checksum = resolvedMigration.getChecksum();
        MigrationType type = appliedMigration.getType().isSynthetic()
                ? appliedMigration.getType()
                : resolvedMigration.getType();

        LOG.info("Repairing metadata for version " + version
                + " (Description: " + description + ", Type: " + type + ", Checksum: " + checksum + ")  ...");

        // Try load an update.sql file if it exists
        String resourceName = "org/flywaydb/core/internal/dbsupport/" + dbSupport.getDbName() + "/update.sql";
        ClassPathResource resource = new ClassPathResource(resourceName, getClass().getClassLoader());
        if (resource.exists()) {
            String source = resource.loadAsString("UTF-8");
            Map<String, String> placeholders = new HashMap<String, String>();

            // Placeholders for column names
            placeholders.put("schema", table.getSchema().getName());
            placeholders.put("table", table.getName());

            // Placeholders for column values
            placeholders.put("version_val", version.toString());
            placeholders.put("description_val", description);
            placeholders.put("type_val", type.name());
            placeholders.put("checksum_val", String.valueOf(checksum));

            String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

            new SqlScript(sourceNoPlaceholders, dbSupport).execute(jdbcTemplate);
        } else {
            try {
                jdbcTemplate.update("UPDATE " + table
                                + " SET "
                                + dbSupport.quote("description") + "=? , "
                                + dbSupport.quote("type") + "=? , "
                                + dbSupport.quote("checksum") + "=?"
                                + " WHERE " + dbSupport.quote("version") + "=?",
                        description, type, checksum, version);
            } catch (SQLException e) {
                throw new FlywaySqlException("Unable to repair metadata table " + table
                        + " for version " + version, e);
            }
        }
    }

    @Override
    public String toString() {
        return table.toString();
    }
}
