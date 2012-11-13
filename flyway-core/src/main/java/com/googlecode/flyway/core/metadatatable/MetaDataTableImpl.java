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

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.TimeFormat;
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
public class MetaDataTableImpl implements MetaDataTable {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MetaDataTableImpl.class);

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
    public MetaDataTableImpl(Connection connection, DbSupport dbSupport, String schema, String table) {
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

        final String source =
                new ClassPathResource(dbSupport.getScriptLocation() + "createMetaDataTable.sql").loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", schema);
        placeholders.put("table", table);
        final String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

        new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
                sqlScript.execute(jdbcTemplate);
                return null;
            }
        });

        LOG.debug("Metadata table created: " + fullyQualifiedMetadataTableName());
    }

    public void createIfNotExists() {
        if (!exists()) {
            create();
        }
    }

    public void lock() {
        try {
            dbSupport.lockTable(schema, table);
        } catch (SQLException e) {
            throw new FlywayException("Unable to lock metadata table (" + fullyQualifiedMetadataTableName() + ")", e);
        }
    }

    public void insert(AppliedMigration appliedMigration) {
        MigrationVersion version = appliedMigration.getVersion();
        try {
            int versionRank = calculateVersionRank(version);

            jdbcTemplate.update("UPDATE " + fullyQualifiedMetadataTableName()
                    + " SET " + dbSupport.quote("version_rank") + " = " + dbSupport.quote("version_rank")
                    + " + 1 WHERE " + dbSupport.quote("version_rank") + " >= ?", versionRank);
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
                    versionRank,
                    calculateInstalledRank(),
                    version.toString(),
                    appliedMigration.getDescription(),
                    appliedMigration.getType().name(),
                    appliedMigration.getScript(),
                    appliedMigration.getChecksum(),
                    appliedMigration.getExecutionTime(),
                    appliedMigration.isSuccess());
        } catch (SQLException e) {
            throw new FlywayException("Unable to insert metadata table row for version " + version, e);
        }
    }

    /**
     * Calculates the installed rank for the new migration to be inserted.
     *
     * @return The installed rank.
     */
    private int calculateInstalledRank() throws SQLException {
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

    public List<AppliedMigration> allAppliedMigrations() {
        if (!exists()) {
            return new ArrayList<AppliedMigration>();
        }

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
                + " FROM " + fullyQualifiedMetadataTableName()
                + " ORDER BY " + dbSupport.quote("version_rank");

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
            throw new FlywayException("Error while retrieving the list of applied migrations", e);
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
        return dbSupport.quote(schema, table);
    }

    public MigrationVersion getCurrentSchemaVersion() {
        if (!hasRows()) {
            return MigrationVersion.EMPTY;
        }

        String query = "SELECT " + dbSupport.quote("version") + " FROM " + fullyQualifiedMetadataTableName() + " WHERE " + dbSupport.quote("version_rank")
                + "IN (SELECT MAX(" + dbSupport.quote("version_rank") + ") FROM " + fullyQualifiedMetadataTableName() + ")";
        try {
            String version = jdbcTemplate.queryForString(query);
            return new MigrationVersion(version);
        } catch (SQLException e) {
            throw new FlywayException("Error determining current schema version", e);
        }
    }

    public void repair() {
        if (!hasFailedMigration()) {
            LOG.info("Repair not necessary. No failed migration detected.");
            return;
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            jdbcTemplate.execute("DELETE FROM " + fullyQualifiedMetadataTableName()
                    + " WHERE " + dbSupport.quote("success") + " = " + dbSupport.getBooleanFalse());
        } catch (SQLException e) {
            throw new FlywayException("Unable to repair metadata table", e);
        }

        stopWatch.stop();

        LOG.info("Metadata successfully repaired (execution time "
                + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
        LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
    }
}
