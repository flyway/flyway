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

package com.google.code.flyway.core;

import com.google.code.flyway.core.dbsupport.DbSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
        SqlScript createMetaDataTableScript = dbSupport.createCreateMetaDataTableScript(tableName);
        createMetaDataTableScript.execute(transactionTemplate, jdbcTemplate);
        LOG.info("Metadata table created: " + tableName);
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
    	if (!exists()) {
    		create();
    	} else {
    		return;
    	}
    	
    	final SchemaVersion version;
    	if (initialVersion == null) {
    		version = new SchemaVersion("0", null);
    	} else {
    		version = initialVersion;
    	}
    	
    	final Migration initialMigration = new Migration() {{
    		schemaVersion = version;
    		scriptName = "<< Flyway Init >>";
    		executionTime = 0;
    		migrationState = MigrationState.SUCCESS;
    	}};
    	
    	transactionTemplate.execute(new TransactionCallback<Void>() {
			@Override
			public Void doInTransaction(TransactionStatus status) {
		    	finishMigration(initialMigration);
				return null;
			}
		});
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
                        + " VALUES (?, ?, ?, ?, ?, 1)", migration.getVersion().getVersion(), migration.getVersion()
                        .getDescription(), migration.getScriptName(), migration.getExecutionTime(), migration
                        .getState().name());
    }

    /**
     * @return The latest migration applied on the schema.
     */
    public Migration latestAppliedMigration() {
        final List<Map<String, Object>> result = jdbcTemplate
                .queryForList("select VERSION, DESCRIPTION, SCRIPT, EXECUTION_TIME, STATE from " + tableName
                        + " where current_version=1");
        if (result.isEmpty()) {
            return new Migration();
        }

        return new Migration() {{
            schemaVersion =
            	new SchemaVersion((String) result.get(0).get("VERSION"), (String) result.get(0).get("DESCRIPTION"));
            migrationState = MigrationState.valueOf((String) result.get(0).get("STATE"));
            executionTime = ((Number) result.get(0).get("EXECUTION_TIME")).intValue();
            scriptName = (String) result.get(0).get("SCRIPT");
        }};
    }

    /**
     * @return Retrieves the number migrations applied to this database.
     */
    public int migrationCount() {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + tableName);
	}
}
