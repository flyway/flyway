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

package com.googlecode.flyway.core.validation;

import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main workflow for validating the applied migrations against the available classpath migrations in order to detect
 * accidental migration changes.
 */
public class DbValidator {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbValidator.class);

    /**
     * The ValidationMode for checksum validation.
     */
    private final ValidationMode validationMode;

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
     *
     * @param validationMode The ValidationMode for checksum validation.
     * @param metaDataTable  Supports reading and writing to the metadata table.
     * @param migrations     All migrations available on the classpath , sorted by version, newest first.
     */
    public DbValidator(ValidationMode validationMode, MetaDataTable metaDataTable, List<Migration> migrations) {
        this.validationMode = validationMode;
        this.metaDataTable = metaDataTable;
        // reverse order
        this.migrations = new ArrayList<Migration>(migrations);
        Collections.reverse(this.migrations);
    }

    /**
     * Validate the checksum of all existing sql migration in the metadata table with the checksum of the sql migrations
     * in the classpath
     *
     * @return description of validation error or NULL if no validation error war found
     */
    public String validate() {
        if (ValidationMode.NONE.equals(validationMode)) {
            return null;
        }

        LOG.debug(String.format("Validating (mode %s) migrations ...", validationMode));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<MetaDataTableRow> appliedMigrations = metaDataTable.allAppliedMigrations();
        if (appliedMigrations.isEmpty()) {
            LOG.info("No migrations applied yet. No validation necessary.");
            return null;
        }

        if (appliedMigrations.size() > migrations.size()) {
            List<SchemaVersion> schemaVersions = new ArrayList<SchemaVersion>();
            for (MetaDataTableRow metaDataTableRow : appliedMigrations) {
                schemaVersions.add(metaDataTableRow.getVersion());
            }
            for (Migration migration : migrations) {
                schemaVersions.remove(migration.getVersion());
            }

            StringBuilder stringBuilder = new StringBuilder();
            boolean first = true;
            for (SchemaVersion schemaVersion : schemaVersions) {
                if (!first) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(schemaVersion);
                first = false;
            }

            return String.format("More applied migrations than classpath migrations: DB=%s, Classpath=%s, missing migrations=(%s)",
                    appliedMigrations.size(), migrations.size(), stringBuilder.toString());
        }

        for (int i = 0; i < appliedMigrations.size(); i++) {
            MetaDataTableRow appliedMigration = appliedMigrations.get(i);
            Migration classpathMigration = migrations.get(i);

            if (!appliedMigration.getVersion().equals(classpathMigration.getVersion())) {
                return String.format("Version mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedMigration.getVersion(), classpathMigration.getVersion());

            }
            if (!appliedMigration.getMigrationType().equals(classpathMigration.getMigrationType())) {
                return String.format("Migration Type mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedMigration.getMigrationType(), classpathMigration.getMigrationType());
            }

            final Integer appliedChecksum = appliedMigration.getChecksum();
            final Integer classpathChecksum = classpathMigration.getChecksum();
            if (!ObjectUtils.nullSafeEquals(appliedChecksum, classpathChecksum)) {
                return String.format("Checksum mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedChecksum, classpathMigration.getChecksum());
            }
        }

        stopWatch.stop();
        if (appliedMigrations.size() == 1) {
            LOG.info(String.format("Validated 1 migration (mode: %s) (execution time %s)",
                    validationMode, TimeFormat.format(stopWatch.getTotalTimeMillis())));
        } else {
            LOG.info(String.format("Validated %d migrations (mode: %s) (execution time %s)",
                    appliedMigrations.size(), validationMode, TimeFormat.format(stopWatch.getTotalTimeMillis())));
        }

        return null;
    }
}
