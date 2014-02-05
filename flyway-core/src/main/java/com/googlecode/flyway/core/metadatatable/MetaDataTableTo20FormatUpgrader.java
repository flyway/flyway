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
import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.resolver.MigrationResolver;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import com.googlecode.flyway.core.util.Resource;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Upgrade the metadata table to Flyway 2.0's format.
 */
public class MetaDataTableTo20FormatUpgrader {
    private static final Log LOG = LogFactory.getLog(MetaDataTableTo20FormatUpgrader.class);

    /**
     * Database-specific support.
     */
    private final DbSupport dbSupport;

    /**
     * For executing operations against the DB.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * The metadata table.
     */
    private final Table table;

    /**
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * Creates a new upgrader.
     *
     * @param dbSupport         Database-specific support.
     * @param table             The metadata table.
     * @param migrationResolver The migration resolver.
     */
    public MetaDataTableTo20FormatUpgrader(DbSupport dbSupport, Table table, MigrationResolver migrationResolver) {
        this.dbSupport = dbSupport;
        this.jdbcTemplate = dbSupport.getJdbcTemplate();
        this.table = table;
        this.migrationResolver = migrationResolver;
    }

    /**
     * Performs the actual upgrade.
     *
     * @throws FlywayException when the upgrade failed.
     */
    public void upgrade() throws FlywayException {
        try {
            if (!needsUpgrade()) {
                LOG.debug("No upgrade to the Flyway 2.0 format necessary for metadata table " + table);
                return;
            }

            LOG.info("Upgrading the metadata table " + table + " to the Flyway 2.0 format...");

            LOG.info("Checking prerequisites...");
            checkPrerequisites();

            LOG.info("Step 1/4: Creating new columns...");
            executePart(1);

            LOG.info("Step 2/4: Populating new columns...");
            addRanks();

            LOG.info("Step 3/4: Tightening constraints...");
            executePart(3);

            LOG.info("Step 4/4: Fixing checksums...");
            fixChecksums();
        } catch (SQLException e) {
            throw new FlywayException("Unable to upgrade the metadata table " + table + " to the Flyway 2.0 format", e);
        }
    }

    /**
     * Fixes the platform encoding dependent checksums.
     */
    private void fixChecksums() throws SQLException {
        Map<MigrationVersion, Integer> correctedChecksums = new HashMap<MigrationVersion, Integer>();

        List<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations();

        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            if (MigrationType.SQL == resolvedMigration.getType()) {
                correctedChecksums.put(resolvedMigration.getVersion(), resolvedMigration.getChecksum());
            }
        }

        for (MigrationVersion version : correctedChecksums.keySet()) {
            jdbcTemplate.execute("UPDATE " + table
                    + " SET " + dbSupport.quote("checksum") + " = ?"
                    + " WHERE " + dbSupport.quote("version") + " = ?",
                    correctedChecksums.get(version), version.toString());

        }
    }

    /**
     * Executes the part script with this number.
     *
     * @param num The number of the part.
     */
    private void executePart(int num) {
        Resource resource = new ClassPathResource(dbSupport.getScriptLocation() + "upgradeTo20FormatPart" + num + ".sql");
        String source = resource.loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", table.getSchema().getName());
        placeholders.put("table", table.getName());
        String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}", true).replacePlaceholders(source);

        SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
        sqlScript.execute(jdbcTemplate);
    }

    /**
     * Adds the values to the rank columns.
     */
    private void addRanks() throws SQLException {
        List<String> versions =
                jdbcTemplate.queryForStringList("SELECT " + dbSupport.quote("version") + " FROM " + table);

        List<MigrationVersion> migrationVersions = new ArrayList<MigrationVersion>(versions.size());
        for (String version : versions) {
            migrationVersions.add(new MigrationVersion(version));
        }

        Collections.sort(migrationVersions);

        for (int i = 0; i < migrationVersions.size(); i++) {
            int rank = i + 1;
            String version = migrationVersions.get(i).toString();
            jdbcTemplate.execute("UPDATE " + table
                    + " SET " + dbSupport.quote("version_rank") + " = ?, " + dbSupport.quote("installed_rank")
                    + " = ? WHERE " + dbSupport.quote("version") + " = ?", rank, rank, version);
        }
    }

    /**
     * Checks whether all prerequisites are met for upgrading the table.
     */
    private void checkPrerequisites() throws SQLException {
        List<String> versions =
                jdbcTemplate.queryForStringList(
                        "select version from " + table.getSchema().getName() + "." + table.getName() + " where description is null");

        if (!versions.isEmpty()) {
            throw new FlywayException(
                    "Unable to upgrade metadata to the new Flyway 2.0 format as description is now mandatory" +
                            " and these migrations do not have one: "
                            + StringUtils.collectionToCommaDelimitedString(versions));
        }
    }

    /**
     * Checks whether the metadata table needs to be upgraded.
     *
     * @return {@code true} if the table need to be upgraded, {@code false} if not.
     */
    private boolean needsUpgrade() throws SQLException {
        return table.existsNoQuotes() && !table.hasColumn("version_rank");
    }
}
