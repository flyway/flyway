/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.runtime;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
     * The name of the schema metadata table used by flyway.
     */
    private final String tableName;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * The transaction template to use.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * Creates a new instance of the metadata table support.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the
     *                            database.
     * @param dbSupport           Database-specific functionality.
     * @param tableName           The name of the schema metadata table used by flyway.
     */
    public MetaDataTable(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                         String tableName) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.tableName = tableName;
    }

    /**
     * Checks whether Flyway's metadata table is already present in the
     * database.
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     */
    private boolean exists() {
        return dbSupport.metaDataTableExists(jdbcTemplate, tableName);
    }

    /**
     * Creates Flyway's metadata table.
     */
    private void create() {
        String location = dbSupport.getCreateMetaDataTableScriptLocation();
        String createMetaDataTableScriptSource = ResourceUtils.loadResourceAsString(location);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", tableName);
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        SqlScript sqlScript = new SqlScript(createMetaDataTableScriptSource, placeholderReplacer);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
        LOG.info("Metadata table created: " + tableName);
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
     * Creates and initializes the Flyway metadata table.
     *
     * @param initialVersion (optional) The initial version to put in the metadata table.
     *                       Only migrations with a version number higher than this one
     *                       will be considered for this database.
     *                       {@code null} defaults the initial version to 0.
     */
    public void init(final SchemaVersion initialVersion) {
        Migration migration = latestAppliedMigration();
        if (migration != null) {
            throw new IllegalStateException("Schema already initialized. Current Version: " + migration.getVersion());
        }

        final Migration initialMigration = new Migration() {{
            schemaVersion = initialVersion;
            scriptName = initialVersion.getDescription();
            executionTime = 0;
            migrationState = MigrationState.SUCCESS;
        }};

        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                finishMigration(initialMigration);
                return null;
            }
        });

        LOG.info("Schema initialized with version: " + initialVersion);
    }

    /**
     * Acquires an exclusive read-write lock on the metadata table. This lock
     * will be released automatically on commit.
     */
    public void lock() {
        if (dbSupport.supportsLocking()) {
            jdbcTemplate.queryForList("SELECT script FROM " + tableName + " FOR UPDATE");
        }
    }

    /**
     * Persists the result of this migration.
     *
     * @param migration The migration that was run.
     */
    public void finishMigration(final Migration migration) {
        jdbcTemplate.update("UPDATE " + tableName + " SET current_version=0");
        jdbcTemplate
                .update("INSERT INTO " + tableName
                        + " (version, description, script, execution_time, state, current_version)"
                        + " VALUES (?, ?, ?, ?, ?, 1)", new Object[]{migration.getVersion().getVersion(), migration.getVersion()
                        .getDescription(), migration.getScriptName(), migration.getExecutionTime(), migration
                        .getState().name()});
    }

    /**
     * @return The latest migration applied on the schema. {@code null} if no migration has been applied so far.
     */
    public Migration latestAppliedMigration() {
        if (!exists()) {
            return null;
        }

        String query = getSelectStatement() + " where current_version=1";
        @SuppressWarnings({"unchecked"})
        final List<Migration> migrations = jdbcTemplate.query(query, new MigrationRowMapper());

        if (migrations.isEmpty()) {
            return null;
        }

        return migrations.get(0);
    }

    /**
     * @return The list of all migrations applied on the schema (oldest first). An empty list if no migration has been
     *         applied so far.
     */
    public List<Migration> allAppliedMigrations() {
        if (!exists()) {
            return new ArrayList<Migration>();
        }

        String query = getSelectStatement();

        @SuppressWarnings({"unchecked"})
        final List<Migration> migrations = jdbcTemplate.query(query, new MigrationRowMapper());

        Collections.sort(migrations);

        return migrations;
    }

    /**
     * @return The select statement for reading the metadata table.
     */
    private String getSelectStatement() {
        return "select VERSION, DESCRIPTION, SCRIPT, EXECUTION_TIME, STATE, INSTALLED_ON from " + tableName;
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
     * Row mapper for Migrations.
     */
    private class MigrationRowMapper implements RowMapper {
        @Override
        public Migration mapRow(final ResultSet rs, int rowNum) throws SQLException {
            return new Migration() {{
                schemaVersion = new SchemaVersion(rs.getString("VERSION"), rs.getString("DESCRIPTION"));
                migrationState = MigrationState.valueOf(rs.getString("STATE"));
                installedOn = rs.getTimestamp("INSTALLED_ON");
                executionTime = toInteger((Number) rs.getObject("EXECUTION_TIME"));
                scriptName = rs.getString("SCRIPT");
            }};
        }
    }
}
