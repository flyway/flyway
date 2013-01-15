/**
 * Copyright (C) 2010-2013 the original author or authors.
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
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.SqlScript;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.util.ClassPathResource;
import com.googlecode.flyway.core.util.PlaceholderReplacer;
import com.googlecode.flyway.core.util.Resource;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Upgrade the metadata table to Flyway 2.0.2's format.
 */
public class MetaDataTableTo202FormatUpgrader {
    private static final Log LOG = LogFactory.getLog(MetaDataTableTo202FormatUpgrader.class);

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
     * Creates a new upgrader.
     *
     * @param dbSupport Database-specific support.
     * @param table     The metadata table.
     */
    public MetaDataTableTo202FormatUpgrader(DbSupport dbSupport, Table table) {
        this.dbSupport = dbSupport;
        this.jdbcTemplate = dbSupport.getJdbcTemplate();
        this.table = table;
    }

    /**
     * Performs the actual upgrade.
     *
     * @throws FlywayException when the upgrade failed.
     */
    public void upgrade() throws FlywayException {
        try {
            if (!needsUpgrade()) {
                LOG.debug("No metadata table upgrade to the Flyway 2.0.2 format necessary");
                return;
            }

            LOG.info("Upgrading the metadata table " + table + " to the Flyway 2.0.2 format...");
            executeScript();
        } catch (SQLException e) {
            throw new FlywayException("Unable to upgrade the metadata table " + table + " to the Flyway 2.0.2 format", e);
        }
    }

    /**
     * Executes the upgrade script.
     */
    private void executeScript() {
        Resource resource = new ClassPathResource(dbSupport.getScriptLocation() + "upgradeTo202Format.sql");
        String source = resource.loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", table.getSchema().getName());
        placeholders.put("table", table.getName());
        String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);

        SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, dbSupport);
        sqlScript.execute(jdbcTemplate);
    }

    /**
     * Checks whether the metadata table needs to be upgraded.
     *
     * @return {@code true} if the table need to be upgraded, {@code false} if not.
     */
    private boolean needsUpgrade() throws SQLException {
        return table.exists() && table.hasPrimaryKey();
    }
}
