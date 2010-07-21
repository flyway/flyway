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
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import java.util.Locale;

/**
 * Main workflow for cleaning the database.
 */
public class DbCleaner {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbMigrator.class);

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The transaction template to use.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new database cleaner.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the
     *                            database.
     * @param dbSupport           Database-specific functionality.
     */
    public DbCleaner(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
    }

    /**
     * Cleans the schema of all objects.
     */
    public void clean() {
        LOG.debug("Starting to drop all database objects ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final SqlScript cleanScript = dbSupport.createCleanScript(jdbcTemplate);
        cleanScript.execute(transactionTemplate, jdbcTemplate);
        stopWatch.stop();
        LOG.info(String.format(Locale.ENGLISH, "Cleaned database schema '%s' (execution time %s)",
                dbSupport.getCurrentSchema(jdbcTemplate), TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }
}
