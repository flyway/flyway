/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.ObjectUtils;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
     * Creates a new database validator.
     *
     * @param validationMode The ValidationMode for checksum validation.
     * @param metaDataTable  Supports reading and writing to the metadata table.
     */
    public DbValidator(ValidationMode validationMode, MetaDataTable metaDataTable) {
        this.validationMode = validationMode;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Validate the checksum of all existing sql migration in the metadata table with the checksum of the sql migrations
     * in the classpath
     *
     * @param resolvedMigrations All migrations available on the classpath, sorted by version, newest first.
     * @return description of validation error or NULL if no validation error was found
     */
    public String validate(List<ExecutableMigration> resolvedMigrations) {
        if (ValidationMode.NONE.equals(validationMode)) {
            return null;
        }

        LOG.debug(String.format("Validating (mode %s) migrations ...", validationMode));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<MetaDataTableRow> appliedMigrations = new ArrayList<MetaDataTableRow>(metaDataTable.allAppliedMigrations());
        if (appliedMigrations.isEmpty()) {
            LOG.info("No migrations applied yet. No validation necessary.");
            return null;
        }

        List<ExecutableMigration> migrations = new ArrayList<ExecutableMigration>(resolvedMigrations);
        // migrations now with newest last
        Collections.reverse(migrations);
        final MetaDataTableRow firstAppliedMigration = appliedMigrations.get(0);
        if (MigrationType.INIT.equals(firstAppliedMigration.getMigrationType())) {
            // if first migration is INIT, just check checksum of following migrations
            final SchemaVersion initVersion = firstAppliedMigration.getVersion();
            appliedMigrations.remove(firstAppliedMigration);

            Iterator<ExecutableMigration> iterator = migrations.iterator();
            while (iterator.hasNext()) {
                ExecutableMigration migration = iterator.next();
                SchemaVersion schemaVersion = new SchemaVersion(migration.getMigrationInfo().getVersion().toString());
                if (schemaVersion.compareTo(initVersion) <= 0) {
                    iterator.remove();
                }
            }
        }

        if (appliedMigrations.size() > migrations.size()) {
            List<MigrationVersion> schemaVersions = new ArrayList<MigrationVersion>();
            for (MetaDataTableRow metaDataTableRow : appliedMigrations) {
                schemaVersions.add(new MigrationVersion(metaDataTableRow.getVersion().toString()));
            }
            for (ExecutableMigration migration : migrations) {
                schemaVersions.remove(migration.getMigrationInfo().getVersion());
            }

            String diff = StringUtils.collectionToCommaDelimitedString(schemaVersions);

            return String.format("More applied migrations than classpath migrations: DB=%s, Classpath=%s, Missing migrations=(%s)",
                    appliedMigrations.size(), migrations.size(), diff);
        }

        for (int i = 0; i < appliedMigrations.size(); i++) {
            MetaDataTableRow appliedMigration = appliedMigrations.get(i);
            //Migrations are sorted in the opposite order: newest first.
            ExecutableMigration classpathMigration = migrations.get(i);

            if (!new MigrationVersion(appliedMigration.getVersion().toString())
                    .equals(classpathMigration.getMigrationInfo().getVersion())) {
                return String.format("Version mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedMigration.getVersion(), classpathMigration.getMigrationInfo().getVersion());

            }
            if (!appliedMigration.getMigrationType().name().equals(classpathMigration.getMigrationInfo().getMigrationType().name())) {
                return String.format("Migration Type mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedMigration.getMigrationType(), classpathMigration.getMigrationInfo().getMigrationType());
            }

            final Integer appliedChecksum = appliedMigration.getChecksum();
            final Integer classpathChecksum = classpathMigration.getMigrationInfo().getChecksum();
            if (!ObjectUtils.nullSafeEquals(appliedChecksum, classpathChecksum)) {
                return String.format("Checksum mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedChecksum, classpathChecksum);
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
