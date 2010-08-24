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

package com.googlecode.flyway.core.metadatatable;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigration;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upgrades older (Flyway <= 0.8.5) metadata tables to the new format (Flyway >= 0.9).
 */
public class MetaDataTable085Upgrader {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MetaDataTable085Upgrader.class);

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
     * The base directory on the classpath where the Sql migrations are located.
     */
    private final String baseDir;

    /**
     * The encoding of Sql migrations.
     */
    private final String encoding;

    /**
     * Creates a new instance of the metadata table support.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the
     *                            database.
     * @param dbSupport           Database-specific functionality.
     * @param tableName           The name of the schema metadata table used by flyway.
     * @param baseDir             The base directory on the classpath where the Sql migrations are located.
     * @param encoding            The encoding of Sql migrations.
     */
    public MetaDataTable085Upgrader(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                                    String tableName, String baseDir, String encoding) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.tableName = tableName;
        this.baseDir = baseDir;
        this.encoding = encoding;
    }

    /**
     * Upgrades the metadata table to the newer format.
     */
    public void upgrade() {
        if (!dbSupport.tableExists(jdbcTemplate, tableName)) {
            // No table present, no need to upgrade
            return;
        }

        if (dbSupport.columnExists(jdbcTemplate, tableName, "checksum")) {
            // checksum column present, table already upgraded
            return;
        }

        LOG.info("Upgrading MetaData table '" + tableName + "' from the old Flyway 0.8.5 format to new Flyway 0.9+ format");

        addColumns();
        migrateData();
        addConstraints();

        LOG.info("MetaData table '" + tableName + "' successfully upgraded");
    }

    /**
     * Adds the new columns to the table.
     */
    private void addColumns() {
        String location = dbSupport.getScriptLocation() + "upgradeMetaDataTable085Columns.sql";
        String scriptSource = ResourceUtils.loadResourceAsString(location);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", tableName);
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        SqlScript sqlScript = new SqlScript(scriptSource, placeholderReplacer);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
    }

    /**
     * Migrates the data from the old format into the new one and fills the new mandatory columns with default values.
     */
    private void migrateData() {
        jdbcTemplate.update("UPDATE " + tableName + " SET type='SQL' where script LIKE 'Sql File:%'");
        jdbcTemplate.update("UPDATE " + tableName + " SET type='JAVA' where script LIKE 'Java Class:%'");

        jdbcTemplate.update("UPDATE " + tableName + " SET installed_by=" + dbSupport.getCurrentUserFunction());

        @SuppressWarnings({"unchecked"})
        List<Map<String, Object>> migrations =
                jdbcTemplate.queryForList("SELECT VERSION, TYPE, SCRIPT FROM " + tableName + " ORDER BY installed_on");

        boolean first = true;
        for (Map<String, Object> migration : migrations) {
            String version = (String) migration.get("VERSION");
            String migrationType = (String) migration.get("TYPE");
            String oldScript = (String) migration.get("SCRIPT");

            String newScript = oldScript.substring(oldScript.indexOf(": ") + ": ".length());
            Integer checksum = null;
            if (MigrationType.SQL.name().equals(migrationType)) {
                ClassPathResource resource = new ClassPathResource(baseDir + "/" + newScript);

                if (first & !resource.exists()) {
                    jdbcTemplate.update("UPDATE " + tableName + " SET type='INIT' where version=?",
                            new Object[]{version});
                    if (newScript.endsWith(".sql")) {
                        newScript = newScript.substring(0, newScript.length() - ".sql".length());
                    }
                } else {
                    checksum = new SqlMigration(resource, null, encoding, "1").getChecksum();
                }
            }
            jdbcTemplate.update("UPDATE " + tableName + " SET script=?, checksum=? where version=?",
                    new Object[]{newScript, checksum, version});
            first = false;
        }
    }

    /**
     * Adds constraints to the new columns.
     */
    private void addConstraints() {
        String location = dbSupport.getScriptLocation() + "upgradeMetaDataTable085Constraints.sql";
        String scriptSource = ResourceUtils.loadResourceAsString(location);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", tableName);
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        SqlScript sqlScript = new SqlScript(scriptSource, placeholderReplacer);
        sqlScript.execute(transactionTemplate, jdbcTemplate);
    }
}
