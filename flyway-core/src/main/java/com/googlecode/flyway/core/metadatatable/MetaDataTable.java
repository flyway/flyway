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
import java.util.Date;
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
            throw new FlywayException("Error checking whether table '" + table + "' exists in schema '" + schema + "'",
                    e);
        }
    }

    /**
     * Creates Flyway's metadata table.
     */
    private void create() {
        LOG.info("Creating Metadata table: " + table + " (Schema: " + schema + ")");

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

        LOG.debug("Metadata table created: " + table + " (Schema: " + schema + ")");
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
            throw new FlywayException("Unable to lock metadata table '" + table + "' in schema '" + schema + "'", e);
        }
    }

    /**
     * Adds this row to the metadata table and mark it as current.
     *
     * @param migrationInfo The migrationInfo to add.
     */
    public void insert(MigrationInfo migrationInfo) {
        try {
            jdbcTemplate.update("UPDATE " + schema + "." + table + " SET current_version=" + dbSupport.getBooleanFalse());
            final String version = migrationInfo.getVersion().toString();
            final String description = abbreviateDescription(migrationInfo.getDescription());
            final String state = migrationInfo.getState().name();
            final String migrationType = migrationInfo.getType().name();
            final Integer checksum = migrationInfo.getChecksum();
            final String script = abbreviateScript(migrationInfo.getScript());
            final Integer executionTime = migrationInfo.getExecutionTime();
            jdbcTemplate.update("INSERT INTO " + schema + "." + table
                    + " (version, description, type, script, checksum, installed_by, execution_time, state, current_version)"
                    + " VALUES (?, ?, ?, ?, ?, " + dbSupport.getCurrentUserFunction() + ", ?, ?, "
                    + dbSupport.getBooleanTrue() + ")",
                    version, description, migrationType, script, checksum, executionTime, state);
        } catch (SQLException e) {
            throw new FlywayException(
                    "Unable to insert metadata table row for version " + migrationInfo.getVersion().toString(), e);
        }
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

        if (description.length() <= 100) {
            return description;
        }

        return description.substring(0, 97) + "...";
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

        if (script.length() <= 200) {
            return script;
        }

        return "..." + script.substring(3, 200);
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
            return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + schema + "." + table) > 0;
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

        String query = getSelectStatement() + " where current_version=" + dbSupport.getBooleanTrue();
        try {
            final List<MigrationInfo> migrationInfos = jdbcTemplate.query(query, new MigrationInfoRowMapper());
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
    public List<MigrationInfo> allAppliedMigrations() {
        if (!exists()) {
            return new ArrayList<MigrationInfo>();
        }

        String query = getSelectStatement();

        try {
            final List<MigrationInfo> migrationInfos = jdbcTemplate.query(query, new MigrationInfoRowMapper());
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
        return "select version as VERSION, description as DESCRIPTION, type as TYPE, script as SCRIPT, checksum as CHECKSUM, installed_on as INSTALLED_ON, execution_time as EXECUTION_TIME, state as STATE from " + schema + "." + table;
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
    public MigrationState getCurrentSchemaState() {
        MigrationInfo latestAppliedMigration = latestAppliedMigration();
        if (latestAppliedMigration == null) {
            return MigrationState.SUCCESS;
        }
        return latestAppliedMigration.getState();
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

        MigrationState result = new TransactionTemplate(connection).execute(new TransactionCallback<MigrationState>() {
            public MigrationState doInTransaction() {
                if (MigrationState.SUCCESS == getCurrentSchemaState()) {
                    LOG.info("Repair not necessary. No failed migration detected.");
                    return null;
                }

                try {
                    jdbcTemplate.execute("delete from " + schema + "." + table + " where version=?",
                            getCurrentSchemaVersion().toString());
                    List<MigrationInfo> migrationInfos = allAppliedMigrations();
                    if (!migrationInfos.isEmpty()) {
                        MigrationInfo migrationInfo = migrationInfos.get(migrationInfos.size() - 1);
                        jdbcTemplate.execute("update " + schema + "." + table + " set current_version=? where version=?",
                                dbSupport.getBooleanTrue(), migrationInfo.getVersion().toString());
                    }
                } catch (SQLException e) {
                    throw new FlywayException("Unable to repair metadata table", e);
                }
                return MigrationState.SUCCESS;
            }
        });

        stopWatch.stop();

        if (MigrationState.SUCCESS == result) {
            LOG.info("Metadata successfully repaired (execution time "
                    + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
            LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
            LOG.info("Current schema version: " + getCurrentSchemaVersion());
        }
    }


    /**
     * Row mapper for Migrations.
     */
    private class MigrationInfoRowMapper implements RowMapper<MigrationInfo> {
        public MigrationInfo mapRow(final ResultSet rs) throws SQLException {
            MigrationVersion version = new MigrationVersion(rs.getString("VERSION"));
            String description = rs.getString("DESCRIPTION");
            MigrationType migrationType = MigrationType.valueOf(rs.getString("TYPE"));
            String script = rs.getString("SCRIPT");
            Integer checksum = toInteger((Number) rs.getObject("CHECKSUM"));

            MigrationInfo migrationInfo = new MigrationInfo(version, description, script, checksum, migrationType);

            Date installedOn = rs.getTimestamp("INSTALLED_ON");
            Integer executionTime = toInteger((Number) rs.getObject("EXECUTION_TIME"));
            MigrationState migrationState = MigrationState.valueOf(rs.getString("STATE"));

            migrationInfo.addExecutionDetails(installedOn, executionTime, migrationState);

            return migrationInfo;
        }
    }
}
