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
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import java.util.Date;

/**
 * A row in the schema metadata table containing information about a migration that has already been applied to a db.
 */
public class MetaDataTableRow {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MetaDataTableRow.class);

    /**
     * The target schema version of this migration.
     */
    protected SchemaVersion schemaVersion = SchemaVersion.EMPTY;

    /**
     * The state of this migration.
     */
    protected MigrationState migrationState = MigrationState.UNKNOWN;

    /**
     * The timestamp when this migration was applied to the database. (Automatically set by the database)
     */
    protected Date installedOn;

    /**
     * The time (in ms) it took to execute.
     */
    protected Integer executionTime;

    /**
     * The script name for the migration history.
     */
    protected String script;

    /**
     * The checksum of the migration.
     * Sql migrations use a crc-32 checksum of the sql script.
     * Java migrations use the SUID or a custom checksum.
     */
    protected Integer checksum;

    /**
     * The type of migration (INIT, SQL or JAVA)
     */
    protected MigrationType migrationType;


    /**
     * @return The type of migration (INIT, SQL or JAVA)
     */
    public MigrationType getMigrationType() {
        return migrationType;
    }

    /**
     * @return The checksum of the migration.
     */
    public Integer getChecksum() {
        return checksum;
    }

    /**
     * @return The schema version after the migration is complete.
     */
    public SchemaVersion getVersion() {
        return schemaVersion;
    }

    /**
     * @return The state of this migration.
     */
    public MigrationState getState() {
        return migrationState;
    }

    /**
     * @return The timestamp when this migration was applied to the database. (Automatically set by the database)
     */
    public Date getInstalledOn() {
        return installedOn;
    }

    /**
     * @return The time (in ms) it took to execute.
     */
    public Integer getExecutionTime() {
        return executionTime;
    }

    /**
     * @return The script name for the migration history.
     */
    public String getScript() {
        return script;
    }

    /**
     * Asserts that this migration has not failed.
     *
     * @throws IllegalStateException Thrown when this migration has failed.
     */
    public void assertNotFailed() {
        if (MigrationState.FAILED == migrationState) {
            throw new IllegalStateException("Migration to version " + schemaVersion
                    + " failed! Please restore backups and roll back database and code!");
        }
    }
}
