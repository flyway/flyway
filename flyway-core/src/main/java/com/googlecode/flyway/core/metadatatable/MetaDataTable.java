/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.MigrationInfoImpl;
import com.googlecode.flyway.core.migration.ResolvedMigration;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import com.googlecode.flyway.core.util.jdbc.RowMapper;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
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
public class MetaDataTable {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MetaDataTable.class);

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The schema in which the metadata table belongs.
     */
    private final String schema;

    /**
     * The name of the schema metadata table used by flyway.
     */
    private final String table;

    /**
     * Connection with ddl manipulation access to the database.
     */
    private final Connection connection;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance of the metadata table support.
     *
     * @param connection Connection with ddl manipulation access to the database.
     * @param dbSupport  Database-specific functionality.
     * @param schema     The schema in which the metadata table belongs.
     * @param table      The name of the schema metadata table used by flyway.
     */
    public MetaDataTable(Connection connection, DbSupport dbSupport, String schema, String table) {
        this.connection = connection;
        this.jdbcTemplate = dbSupport.getJdbcTemplate();
        this.dbSupport = dbSupport;
        this.schema = schema;
        this.table = table;
    }

    /**
     * Checks whether Flyway's metadata table is already present in the database.
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    private boolean exists() {
        try {
            return dbSupport.tableExists(schema, table);
        } catch (SQLException e) {
            throw new FlywayException("Error checking whether metadata table (" + fullyQualifiedMetadataTableName() + ") exists",
                    e);
        }
    }

    /**
     * Creates Flyway's metadata table.
     */
    private void create() {
        LOG.info("Creating Metadata table: " + fullyQualifiedMetadataTableName());

        final String createMetaDataTableScriptSource =
                new ClassPathResource(dbSupport.getScriptLocation() + "createMetaDataTable.sql").loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", schema);
        placeholders.put("table", table);
        final PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                SqlScript sqlScript = new SqlScript(createMetaDataTableScriptSource, placeholderReplacer, dbSupport);
                sqlScript.execute(jdbcTemplate);
                return null;
            }
        });

        LOG.debug("Metadata table created: " + fullyQualifiedMetadataTableName());
    }

    /**
     * Creates the metadata table if it doesn't already exist.
     */
    public void createIfNotExists() {
        if (!exists()) {
            create();
        }
    }

    /**
     * Acquires an exclusive read-write lock on the metadata table. This lock will be released automatically on commit.
     */
    public void lock() {
        try {
            dbSupport.lockTable(schema, table);
        } catch (SQLException e) {
            throw new FlywayException("Unable to lock metadata table (" + fullyQualifiedMetadataTableName() + ")", e);
        }
    }

    /**
     * Adds this migration as executed to the metadata table and mark it as current.
     *
     * @param resolvedMigration The migrationInfo to add.
     * @param success           Whether the migration was successfully executed.
     * @param executionTime     The time in millis it took to execute the migration.
     */
    public void insert(ResolvedMigration resolvedMigration, boolean success, int executionTime) {
        try {
            MigrationVersion version = resolvedMigration.getVersion();
            int versionRank = calculateVersionRank(version);
            int installedRank = caculateInstalledRank();
            String description = abbreviateDescription(resolvedMigration.getDescription());
            String migrationType = resolvedMigration.getType().name();
            Integer checksum = resolvedMigration.getChecksum();
            String script = abbreviateScript(resolvedMigration.getScript());

            jdbcTemplate.update("INSERT INTO " + fullyQualifiedMetadataTableName()
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
                    versionRank, installedRank, version.toString(), description, migrationType, script, checksum,
                    executionTime, success);
        } catch (SQLException e) {
            throw new FlywayException("Unable to insert metadata table row for version " + resolvedMigration.getVersion(), e);
        }
    }

    /**
     * Calculates the installed rank for the new migration to be inserted.
     *
     * @return The installed rank.
     */
    private int caculateInstalledRank() throws SQLException {
        int currentMax = jdbcTemplate.queryForInt("SELECT MAX(" + dbSupport.quote("installed_rank") + ")"
                + " FROM " + fullyQualifiedMetadataTableName());
        return currentMax + 1;
    }

    /**
     * Calculate the rank for this new version about to be inserted.
     *
     * @param version The version to calculated for.
     * @return The rank.
     */
    private int calculateVersionRank(MigrationVersion version) throws SQLException {
        List<String> versions = jdbcTemplate.queryForStringList("select " + dbSupport.quote("version") + " from " + fullyQualifiedMetadataTableName());

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

    /**
     * Abbreviates this description to a length that will fit in the database.
     *
     * @param description The description to process.
     * @return The abbreviated version.
     */
    private String abbreviateDescription(String description) {
        if (description == null) {
            return null;
        }

        if (description.length() <= 200) {
            return description;
        }

        return description.substring(0, 197) + "...";
    }

    /**
     * Abbreviates this script to a length that will fit in the database.
     *
     * @param script The script to process.
     * @return The abbreviated version.
     */
    private String abbreviateScript(String script) {
        if (script == null) {
            return null;
        }

        if (script.length() <= 1000) {
            return script;
        }

        return "..." + script.substring(3, 1000);
    }

    /**
     * Checks whether the metadata table contains at least one row.
     *
     * @return {@code true} if the metadata table has at least one row. {@code false} if it is empty or it doesn't exist
     *         yet.
     */
    private boolean hasRows() {
        if (!exists()) {
            return false;
        }

        try {
            return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + fullyQualifiedMetadataTableName()) > 0;
        } catch (SQLException e) {
            throw new FlywayException("Error checking if the metadata table has at least one row", e);
        }
    }

    /**
     * @return The latest migration applied on the schema. {@code null} if no migration has been applied so far.
     */
    public MigrationInfo latestAppliedMigration() {
        if (!hasRows()) {
            return null;
        }

        String query = getSelectStatement() + " WHERE " + dbSupport.quote("version_rank")
                + "IN (SELECT MAX(" + dbSupport.quote("version_rank") + ") FROM " + fullyQualifiedMetadataTableName() + ")";
        try {
            final List<? extends MigrationInfo> migrationInfos = jdbcTemplate.query(query, new MigrationInfoRowMapper());
            if (migrationInfos.isEmpty()) {
                throw new FlywayException("Cannot determine latest applied migration. Was the metadata table manually modified?");
            }
            return migrationInfos.get(0);
        } catch (SQLException e) {
            throw new FlywayException("Error determining latest applied migration", e);
        }
    }

    /**
     * @return The list of all migrations applied on the schema (oldest first). An empty list if no migration has been
     *         applied so far.
     */
    public List<MigrationInfoImpl> allAppliedMigrations() {
        if (!exists()) {
            return new ArrayList<MigrationInfoImpl>();
        }

        String query = getSelectStatement();

        try {
            final List<MigrationInfoImpl> migrationInfos = jdbcTemplate.query(query, new MigrationInfoRowMapper());
            Collections.sort(migrationInfos);
            return migrationInfos;
        } catch (SQLException e) {
            throw new FlywayException("Error while retrieving the list of applied migrations", e);
        }
    }

    /**
     * @return The select statement for reading the metadata table.
     */
    private String getSelectStatement() {
        return "SELECT " + dbSupport.quote("version")
                + "," + dbSupport.quote("description")
                + "," + dbSupport.quote("type")
                + "," + dbSupport.quote("script")
                + "," + dbSupport.quote("checksum")
                + "," + dbSupport.quote("installed_on")
                + "," + dbSupport.quote("execution_time")
                + "," + dbSupport.quote("success")
                + " FROM " + fullyQualifiedMetadataTableName();
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


    /**
     * @return The current state of the schema. {@code MigrationState.SUCCESS} for an empty schema.
     */
    public boolean hasFailedMigration() {
        if (!exists()) {
            return false;
        }

        try {
            int failedCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + fullyQualifiedMetadataTableName()
                    + " WHERE " + dbSupport.quote("success") + "=" + dbSupport.getBooleanFalse());
            return failedCount > 0;
        } catch (SQLException e) {
            throw new FlywayException("Unable to check the metadata table (" + fullyQualifiedMetadataTableName() + ") for failed migrations", e);
        }
    }

    /**
     * @return The fully qualified name of the metadata table, including the schema it is contained in.
     */
    private String fullyQualifiedMetadataTableName() {
        return dbSupport.quote(schema) + "." + dbSupport.quote(table);
    }

    /**
     * @return The current version of the schema. {@code MigrationVersion.EMPTY} for an empty schema.
     */
    public MigrationVersion getCurrentSchemaVersion() {
        MigrationInfo latestAppliedMigration = latestAppliedMigration();
        if (latestAppliedMigration == null) {
            return MigrationVersion.EMPTY;
        }
        return new MigrationVersion(latestAppliedMigration.getVersion().toString());
    }

    /**
     * <p>
     * Repairs the metadata table after a failed migration.
     * This is only necessary for databases without DDL-transaction support.
     * </p>
     * <p>
     * On databases with DDL transaction support, a migration failure automatically triggers a rollback of all changes,
     * including the ones in the metadata table.
     * </p>
     */
    public void repair() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                if (!hasFailedMigration()) {
                    LOG.info("Repair not necessary. No failed migration detected.");
                    return null;
                }

                try {
                    jdbcTemplate.execute("DELETE FROM " + fullyQualifiedMetadataTableName() + " WHERE " + dbSupport.quote("success") + " = " + dbSupport.getBooleanFalse());
                } catch (SQLException e) {
                    throw new FlywayException("Unable to repair metadata table", e);
                }
                return null;
            }
        });

        stopWatch.stop();

        LOG.info("Metadata successfully repaired (execution time "
                + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
        LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
        LOG.info("Current schema version: " + getCurrentSchemaVersion());
    }


    /**
     * Row mapper for Migrations.
     */
    private class MigrationInfoRowMapper implements RowMapper<MigrationInfoImpl> {
        public MigrationInfoImpl mapRow(final ResultSet rs) throws SQLException {
            MigrationVersion version = new MigrationVersion(rs.getString("version"));
            String description = rs.getString("description");
            MigrationType migrationType = MigrationType.valueOf(rs.getString("type"));
            String script = rs.getString("script");
            Integer checksum = toInteger((Number) rs.getObject("checksum"));

            MigrationInfoImpl migrationInfo = new MigrationInfoImpl(version, description, script, checksum, migrationType);
            migrationInfo.setInstalledOn(rs.getTimestamp("installed_on"));
            migrationInfo.setExecutionTime(toInteger((Number) rs.getObject("execution_time")));
            boolean success = rs.getBoolean("success");
            if (success) {
                migrationInfo.setState(MigrationState.SUCCESS);
            } else {
                migrationInfo.setState(MigrationState.FAILED);
            }

            return migrationInfo;
        }
    }
}
