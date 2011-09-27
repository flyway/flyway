/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the database.
     * @param dbSupport           Database-specific functionality.
     * @param schema              The schema in which the metadata table belongs.
     * @param table           The name of the schema metadata table used by flyway.
     */
    public MetaDataTable(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                         String schema, String table) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
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
        return dbSupport.tableExists(schema, table);
    }

    /**
     * Creates Flyway's metadata table.
     */
    private void create() {
        String location = dbSupport.getScriptLocation() + "createMetaDataTable.sql";
        final String createMetaDataTableScriptSource = ResourceUtils.loadResourceAsString(location);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", schema);
        placeholders.put("table", table);
        final PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                SqlScript sqlScript = new SqlScript(createMetaDataTableScriptSource, placeholderReplacer);
                sqlScript.execute(jdbcTemplate);
            }
        });

        LOG.info("Metadata table created: " + table + " (Schema: " + schema + ")");
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
        dbSupport.lockTable(schema, table);
    }

    /**
     * Adds this row to the metadata table and mark it as current.
     *
     * @param metaDataTableRow The metaDataTableRow to add.
     */
    public void insert(final MetaDataTableRow metaDataTableRow) {
        jdbcTemplate.update("UPDATE " + schema + "." + table + " SET current_version=" + dbSupport.getBooleanFalse());
        final String version = metaDataTableRow.getVersion().toString();
        final String description = metaDataTableRow.getDescription();
        final String state = metaDataTableRow.getState().name();
        final String migrationType = metaDataTableRow.getMigrationType().name();
        final Integer checksum = metaDataTableRow.getChecksum();
        final String scriptName = metaDataTableRow.getScript();
        final Integer executionTime = metaDataTableRow.getExecutionTime();
        jdbcTemplate.update("INSERT INTO " + schema + "." + table
                + " (version, description, type, script, checksum, installed_by, execution_time, state, current_version)"
                + " VALUES (?, ?, ?, ?, ?, " + dbSupport.getCurrentUserFunction() + ", ?, ?, "
                + dbSupport.getBooleanTrue() + ")",
                new Object[]{version, description, migrationType, scriptName, checksum, executionTime, state});
    }

    /**
     * Checks whether the metadata table contains at least one row.
     *
     * @return {@code true} if the metadata table has at least on row. {@code false} if it is empty or it doesn't exist
     *         yet.
     */
    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean hasRows() {
        if (!exists()) {
            return false;
        }

        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + schema + "." + table) > 0;
    }

    /**
     * @return The latest migration applied on the schema. {@code null} if no migration has been applied so far.
     */
    public MetaDataTableRow latestAppliedMigration() {
        if (!hasRows()) {
            return null;
        }

        String query = getSelectStatement() + " where current_version=" + dbSupport.getBooleanTrue();
        @SuppressWarnings({"unchecked"})
        final List<MetaDataTableRow> metaDataTableRows = jdbcTemplate.query(query, new MetaDataTableRowMapper());

        if (metaDataTableRows.isEmpty()) {
            if (hasRows()) {
                throw new FlywayException("Cannot determine latest applied migration. Was the metadata table manually modified?");
            }
            return null;
        }

        return metaDataTableRows.get(0);
    }

    /**
     * @return The list of all migrations applied on the schema (oldest first). An empty list if no migration has been
     *         applied so far.
     */
    public List<MetaDataTableRow> allAppliedMigrations() {
        if (!exists()) {
            return new ArrayList<MetaDataTableRow>();
        }

        String query = getSelectStatement();

        @SuppressWarnings({"unchecked"})
        final List<MetaDataTableRow> metaDataTableRows = jdbcTemplate.query(query, new MetaDataTableRowMapper());

        Collections.sort(metaDataTableRows);

        return metaDataTableRows;
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
     *
     * @return The matching Integer.
     */
    private Integer toInteger(Number number) {
        if (number == null) {
            return null;
        }

        return number.intValue();
    }


    /**
     * @return The current state of the schema.
     */
    public MigrationState getCurrentSchemaState() {
        MetaDataTableRow latestAppliedMigration = latestAppliedMigration();
        if (latestAppliedMigration == null) {
            return MigrationState.SUCCESS;
        }
        return latestAppliedMigration.getState();
    }

    /**
     * @return The current version of the schema.
     */
    public SchemaVersion getCurrentSchemaVersion() {
        MetaDataTableRow latestAppliedMigration = latestAppliedMigration();
        if (latestAppliedMigration == null) {
            return SchemaVersion.EMPTY;
        }
        return latestAppliedMigration.getVersion();
    }


    /**
     * Row mapper for Migrations.
     */
    private class MetaDataTableRowMapper implements RowMapper {
        public MetaDataTableRow mapRow(final ResultSet rs, int rowNum) throws SQLException {
            SchemaVersion version = new SchemaVersion(rs.getString("VERSION"));
            String description = rs.getString("DESCRIPTION");
            MigrationType migrationType = MigrationType.valueOf(rs.getString("TYPE"));
            String script = rs.getString("SCRIPT");
            Integer checksum = toInteger((Number) rs.getObject("CHECKSUM"));
            Date installedOn = rs.getTimestamp("INSTALLED_ON");
            Integer executionTime = toInteger((Number) rs.getObject("EXECUTION_TIME"));
            MigrationState migrationState = MigrationState.valueOf(rs.getString("STATE"));

            return new MetaDataTableRow(version, description, migrationType, script, checksum, installedOn, executionTime, migrationState);
        }
    }
}
