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

import com.googlecode.flyway.core.ValidationType;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Main workflow for validating the applied migrations with classpath migrations
 * in order to detect accidental changes migrations.
 */
public class DbValidator {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbValidator.class);

    /**
     * The ValidationType for checksum validation.
     */
    private final ValidationType validationType;

    /**
     * Supports reading and writing to the metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * All available classpath migrations, sorted by version, newest last.
     */
    private final List<Migration> migrations;

    /**
     * Creates a new database validator.
     * @param validationType The ValidationType for checksum validation.
     * @param metaDataTable Supports reading and writing to the metadata table.
     * @param migrations All available classpath migrations, sorted by version, newest first.
     */
    public DbValidator(ValidationType validationType, MetaDataTable metaDataTable, List<Migration> migrations) {
        this.validationType = validationType;
        this.metaDataTable = metaDataTable;
        // reverse order
        this.migrations = new ArrayList<Migration>(migrations);
        Collections.reverse(this.migrations);
    }

    /**
     * Validate the checksum of all existing sql migration in the metadata table
     * with the checksum of the sql migrations in the classpath
     * @return description of validation error or NULL if no validation error war found
     */
    public String validate() {
        if (!validationType.isAll() || !metaDataTable.exists()) {
            return null;
        }

        LOG.debug(String.format("Starting to validate (mode %s) migrations ...", validationType));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<Migration> appliedMigrations = metaDataTable.allAppliedMigrations();
        if (appliedMigrations.size() > migrations.size()) {
            return String.format("more applied migrations than classpath migrations: applied migrations=%s, classpath migrations=%s",
                    appliedMigrations.size(), migrations.size());
        }

        for (int i = 0; i < appliedMigrations.size(); i++) {
            Migration appliedMigration = appliedMigrations.get(i);
            Migration classpathMigration = migrations.get(i);
            if (appliedMigration.getVersion().compareTo(classpathMigration.getVersion()) != 0) {
                return String.format("different schema version in migration %s: applied migrations=%s, classpath migrations=%s",
                        i, appliedMigration.getVersion().getVersion(), classpathMigration.getVersion().getVersion());

            }
            if (!appliedMigration.getMigrationType().equals(classpathMigration.getMigrationType())) {
                return String.format("different migration type in migration %s: applied migrations=%s, classpath migrations=%s",
                        i, appliedMigration.getMigrationType(), classpathMigration.getMigrationType());
            }
            if (appliedMigration.getMigrationType().equals(MigrationType.SQL)) {
                if (!appliedMigration.getChecksum().equals(classpathMigration.getChecksum())) {
                    return String.format("different checksum for sql migration %s: applied migrations=%s, classpath migrations=%s",
                            i, appliedMigration.getChecksum(), classpathMigration.getChecksum());
                }
            }
        }

        stopWatch.stop();
        LOG.info(String.format(Locale.ENGLISH, "Validated (mode %s) migrations (execution time %s)",
                validationType, TimeFormat.format(stopWatch.getTotalTimeMillis())));

        return null;
    }
}
