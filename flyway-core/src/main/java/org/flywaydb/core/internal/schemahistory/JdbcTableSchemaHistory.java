/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.jdbc.JdbcNullTypes;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Supports reading and writing to the schema history table.
 */
@CustomLog
class JdbcTableSchemaHistory extends SchemaHistory {
    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
    private final SqlScriptFactory sqlScriptFactory;

    /**
     * The database to use.
     */
    private final Database database;

    /**
     * Connection with access to the database.
     */
    private final Connection<?> connection;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Applied migration cache.
     */
    private final LinkedList<AppliedMigration> cache = new LinkedList<>();

    private final Configuration configuration;

    /**
     * Creates a new instance of the schema history table support.
     *
     * @param database The database to use.
     * @param table The schema history table used by Flyway.
     */
    JdbcTableSchemaHistory(SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory,
                           Database database, Table table, Configuration configuration) {
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.sqlScriptFactory = sqlScriptFactory;
        this.table = table;
        this.database = database;
        this.connection = database.getMainConnection();
        this.jdbcTemplate = connection.getJdbcTemplate();
        this.configuration = configuration;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public boolean exists() {
        connection.restoreOriginalState();

        return table.exists();
    }

    @Override
    public void create(final boolean baseline) {
        connection.lock(table, new Callable<Object>() {
            @Override
            public Object call() {
                int retries = 0;
                while (!exists()) {
                    if (retries == 0) {
                        LOG.info("Creating Schema History table " + table + (baseline ? " with baseline" : "") + " ...");
                    }
                    try {
                        ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                                                                         database).execute(new Callable<Object>() {
                            @Override
                            public Object call() {
                                sqlScriptExecutorFactory.createSqlScriptExecutor(connection.getJdbcConnection(), false, false, true)
                                        .execute(database.getCreateScript(sqlScriptFactory, table, baseline));
                                LOG.debug("Created Schema History table " + table + (baseline ? " with baseline" : ""));
                                return null;
                            }
                        });
                    } catch (FlywayException e) {
                        if (++retries >= 10) {
                            throw e;
                        }
                        try {
                            LOG.debug("Schema History table creation failed. Retrying in 1 sec ...");
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            // Ignore
                        }
                    }
                }
                return null;
            }
        });
    }

    @Override
    public <T> T lock(Callable<T> callable) {
        connection.restoreOriginalState();

        return connection.lock(table, callable);
    }

    @Override
    protected void doAddAppliedMigration(int installedRank, MigrationVersion version, String description,
                                         MigrationType type, String script, Integer checksum,
                                         int executionTime, boolean success) {
        boolean tableIsLocked = false;
        connection.restoreOriginalState();

        // Lock again for databases with no clean DDL transactions like Oracle
        // to prevent implicit commits from triggering deadlocks
        // in highly concurrent environments
        if (!database.supportsDdlTransactions()) {
            table.lock();
            tableIsLocked = true;
        }

        try {
            String versionStr = version == null ? null : version.toString();

            if (!database.supportsEmptyMigrationDescription() && "".equals(description)) {
                description = NO_DESCRIPTION_MARKER;
            }

            Object versionObj = versionStr == null ? JdbcNullTypes.StringNull : versionStr;
            Object checksumObj = checksum == null ? JdbcNullTypes.IntegerNull : checksum;

            jdbcTemplate.update(database.getInsertStatement(table),
                                installedRank, versionObj, description, type.name(), script, checksumObj, database.getInstalledBy(),
                                executionTime, success);

            LOG.debug("Schema History table " + table + " successfully updated to reflect changes");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to insert row for version '" + version + "' in Schema History table " + table, e);
        } finally {
            if (tableIsLocked) {
                table.unlock();
            }
        }
    }

    @Override
    public List<AppliedMigration> allAppliedMigrations() {
        if (!exists()) {
            LOG.info(String.format("Schema history table %s does not exist yet", table.toString()));
            return new ArrayList<>();
        }

        refreshCache();
        return cache;
    }

    private void refreshCache() {
        int maxCachedInstalledRank = cache.isEmpty() ? -1 : cache.getLast().getInstalledRank();
        String query = database.getSelectStatement(table);

        try {
            cache.addAll(jdbcTemplate.query(query, rs -> {
                // Construct a map of lower-cased column names to ordinals. This is useful for databases that
                // upper-case them - e.g. Snowflake with QUOTED-IDENTIFIERS-IGNORE-CASE turned on
                HashMap<String, Integer> columnOrdinalMap = constructColumnOrdinalMap(rs);

                Integer checksum = rs.getInt(columnOrdinalMap.get("checksum"));
                if (rs.wasNull()) {
                    checksum = null;
                }

                int installedRank = rs.getInt(columnOrdinalMap.get("installed_rank"));
                MigrationVersion version = rs.getString(columnOrdinalMap.get("version")) != null ? MigrationVersion.fromVersion(rs.getString(columnOrdinalMap.get("version"))) : null;
                String description = rs.getString(columnOrdinalMap.get("description"));
                String type = rs.getString(columnOrdinalMap.get("type"));
                String script = rs.getString(columnOrdinalMap.get("script"));
                Timestamp installedOn = rs.getTimestamp(columnOrdinalMap.get("installed_on"));
                String installedBy = rs.getString(columnOrdinalMap.get("installed_by"));
                int executionTime = rs.getInt(columnOrdinalMap.get("execution_time"));
                boolean success = rs.getBoolean(columnOrdinalMap.get("success"));

                return configuration.getPluginRegister().getPlugins(AppliedMigration.class).stream()
                        .filter(am -> am.handlesType(type))
                        .findFirst()
                        .orElse(new BaseAppliedMigration())
                        .create(installedRank, version, description, type, script, checksum, installedOn, installedBy, executionTime, success);
            }, maxCachedInstalledRank));
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while retrieving the list of applied migrations from Schema History table " + table, e);
        }
    }

    private HashMap<String, Integer> constructColumnOrdinalMap(ResultSet rs) throws SQLException {
        HashMap<String, Integer> columnOrdinalMap = new HashMap<>();
        ResultSetMetaData metadata = rs.getMetaData();

        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            // Careful - column ordinals in JDBC start at 1
            String columnNameLower = metadata.getColumnName(i).toLowerCase();
            columnOrdinalMap.put(columnNameLower, i);
        }
        return columnOrdinalMap;
    }

    @Override
    public boolean removeFailedMigrations(RepairResult repairResult, MigrationPattern[] migrationPatternFilter) {
        if (!exists()) {
            LOG.info("Repair of failed migration in Schema History table " + table + " not necessary as table doesn't exist.");
            return false;
        }

        List<AppliedMigration> appliedMigrations = filterMigrations(allAppliedMigrations(), migrationPatternFilter);

        boolean failed = appliedMigrations.stream().anyMatch(am -> !am.isSuccess());
        if (!failed) {
            LOG.info("Repair of failed migration in Schema History table " + table + " not necessary. No failed migration detected.");
            return false;
        }

        try {
            appliedMigrations.stream()
                    .filter(am -> !am.isSuccess())
                    .forEach(am -> repairResult.migrationsRemoved.add(CommandResultFactory.createRepairOutput(am)));

            for (AppliedMigration appliedMigration : appliedMigrations) {
                jdbcTemplate.execute("DELETE FROM " + table +
                                             " WHERE " + database.quote("success") + " = " + database.getBooleanFalse() + " AND " +
                                             (appliedMigration.getVersion() != null ?
                                                     database.quote("version") + " = '" + appliedMigration.getVersion().getVersion() + "'" :
                                                     database.quote("description") + " = '" + appliedMigration.getDescription() + "'"));
            }

            clearCache();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair Schema History table " + table, e);
        }

        return true;
    }

    private List<AppliedMigration> filterMigrations(List<AppliedMigration> appliedMigrations, MigrationPattern[] migrationPatternFilter) {
        if (migrationPatternFilter == null) {
            return appliedMigrations;
        }

        Set<AppliedMigration> filteredList = new HashSet<>();

        for (AppliedMigration appliedMigration : appliedMigrations) {
            for (MigrationPattern migrationPattern : migrationPatternFilter) {
                if (migrationPattern.matches(appliedMigration.getVersion(), appliedMigration.getDescription())) {
                    filteredList.add(appliedMigration);
                }
            }
        }

        return new ArrayList<>(filteredList);
    }

    @Override
    public void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration) {
        connection.restoreOriginalState();

        clearCache();

        MigrationVersion version = appliedMigration.getVersion();

        String description = resolvedMigration.getDescription();
        Integer checksum = resolvedMigration.getChecksum();
        MigrationType type = appliedMigration.getType().isSynthetic()
                ? appliedMigration.getType()
                : resolvedMigration.getType();

        LOG.info("Repairing Schema History table for version " + version
                         + " (Description: " + description + ", Type: " + type + ", Checksum: " + checksum + ")  ...");

        if (!database.supportsEmptyMigrationDescription() && "".equals(description)) {
            description = NO_DESCRIPTION_MARKER;
        }

        Object checksumObj = checksum == null ? JdbcNullTypes.IntegerNull : checksum;

        try {
            jdbcTemplate.update("UPDATE " + table
                                        + " SET "
                                        + database.quote("description") + "=? , "
                                        + database.quote("type") + "=? , "
                                        + database.quote("checksum") + "=?"
                                        + " WHERE " + database.quote("installed_rank") + "=?",
                                description, type.name(), checksumObj, appliedMigration.getInstalledRank());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair Schema History table " + table
                                                 + " for version " + version, e);
        }
    }

    @Override
    public void delete(AppliedMigration appliedMigration) {
        connection.restoreOriginalState();

        clearCache();

        MigrationVersion version = appliedMigration.getVersion();
        String versionStr = version == null ? null : version.toString();

        if (version == null) {
            LOG.info("Repairing Schema History table for description \"" + appliedMigration.getDescription() + "\" (Marking as DELETED)  ...");
        } else {
            LOG.info("Repairing Schema History table for version \"" + version + "\" (Marking as DELETED)  ...");
        }

        Object versionObj = versionStr == null ? JdbcNullTypes.StringNull : versionStr;
        Object checksumObj = appliedMigration.getChecksum() == null ? JdbcNullTypes.IntegerNull : appliedMigration.getChecksum();

        try {
            jdbcTemplate.update(database.getInsertStatement(table),
                                calculateInstalledRank(appliedMigration.getType()),
                                versionObj, appliedMigration.getDescription(), "DELETE", appliedMigration.getScript(),
                                checksumObj, database.getInstalledBy(), 0, appliedMigration.isSuccess());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair Schema History table " + table
                                                 + " for version " + version, e);
        }
    }
}