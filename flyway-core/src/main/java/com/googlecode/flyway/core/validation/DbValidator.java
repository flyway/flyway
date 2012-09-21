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

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.util.ObjectUtils;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main workflow for validating the applied migrations against the available classpath migrations in order to detect
 * accidental migration changes.
 */
public class DbValidator {
    private static final Log LOG = LogFactory.getLog(DbValidator.class);

    /**
     * Supports reading and writing to the metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * Creates a new database validator.
     *
     * @param metaDataTable Supports reading and writing to the metadata table.
     */
    public DbValidator(MetaDataTable metaDataTable) {
        this.metaDataTable = metaDataTable;
    }

    /**
     * Validate the checksum of all existing sql migration in the metadata table with the checksum of the sql migrations
     * in the classpath
     *
     * @param migrations All migrations available on the classpath, sorted by version, newest first.
     * @return description of validation error or NULL if no validation error was found
     */
    public String validate(List<? extends MigrationInfo> migrations) {
        LOG.debug("Validating migrations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<MigrationInfo> appliedMigrations = new ArrayList<MigrationInfo>(metaDataTable.allAppliedMigrations());
        if (appliedMigrations.isEmpty()) {
            LOG.info("No migrations applied yet. No validation necessary.");
            return null;
        }

        final MigrationInfo firstAppliedMigration = appliedMigrations.get(0);
        if (com.googlecode.flyway.core.api.MigrationType.INIT.equals(firstAppliedMigration.getType())) {
            // if first migration is INIT, just check checksum of following migrations
            final MigrationVersion initVersion = firstAppliedMigration.getVersion();
            appliedMigrations.remove(firstAppliedMigration);

            Iterator<? extends MigrationInfo> iterator = migrations.iterator();
            while (iterator.hasNext()) {
                MigrationInfo migration = iterator.next();
                if (migration.getVersion().compareTo(initVersion) <= 0) {
                    iterator.remove();
                }
            }
        }

        if (appliedMigrations.size() > migrations.size()) {
            List<MigrationVersion> schemaVersions = new ArrayList<MigrationVersion>();
            for (MigrationInfo metaDataTableRow : appliedMigrations) {
                schemaVersions.add(new MigrationVersion(metaDataTableRow.getVersion().toString()));
            }
            for (MigrationInfo migration : migrations) {
                schemaVersions.remove(migration.getVersion());
            }

            String diff = StringUtils.collectionToCommaDelimitedString(schemaVersions);

            return String.format("More applied migrations than classpath migrations: DB=%s, Classpath=%s, Missing migrations=(%s)",
                    appliedMigrations.size(), migrations.size(), diff);
        }

        for (int i = 0; i < appliedMigrations.size(); i++) {
            MigrationInfo appliedMigration = appliedMigrations.get(i);
            //Migrations are sorted in the opposite order: newest first.
            MigrationInfo classpathMigration = migrations.get(i);

            if (!new MigrationVersion(appliedMigration.getVersion().toString())
                    .equals(classpathMigration.getVersion())) {
                return String.format("Version mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedMigration.getVersion(), classpathMigration.getVersion());

            }
            if (!appliedMigration.getType().equals(classpathMigration.getType())) {
                return String.format("Migration Type mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedMigration.getType(), classpathMigration.getType());
            }

            final Integer appliedChecksum = appliedMigration.getChecksum();
            final Integer classpathChecksum = classpathMigration.getChecksum();
            if (!ObjectUtils.nullSafeEquals(appliedChecksum, classpathChecksum)) {
                return String.format("Checksum mismatch for migration %s: DB=%s, Classpath=%s",
                        appliedMigration.getScript(), appliedChecksum, classpathChecksum);
            }
        }

        stopWatch.stop();
        if (appliedMigrations.size() == 1) {
            LOG.info(String.format("Validated 1 migration (execution time %s)",
                    TimeFormat.format(stopWatch.getTotalTimeMillis())));
        } else {
            LOG.info(String.format("Validated %d migrations (execution time %s)",
                    appliedMigrations.size(), TimeFormat.format(stopWatch.getTotalTimeMillis())));
        }

        return null;
    }
}
