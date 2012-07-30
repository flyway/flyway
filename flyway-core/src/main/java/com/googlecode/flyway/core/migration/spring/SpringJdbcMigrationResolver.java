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
package com.googlecode.flyway.core.migration.spring;

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.api.migration.MigrationChecksumProvider;
import com.googlecode.flyway.core.api.migration.MigrationInfoProvider;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationExecutor;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.util.ClassUtils;
import com.googlecode.flyway.core.util.scanner.ClassPathScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration resolver for Spring Jdbc migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 */
public class SpringJdbcMigrationResolver implements MigrationResolver {
    /**
     * The base package on the classpath where to migrations are located.
     */
    private final String basePackage;

    /**
     * Creates a new instance.
     *
     * @param basePackage The base package on the classpath where to migrations are located.
     */
    public SpringJdbcMigrationResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<ExecutableMigration> resolveMigrations() {
        List<ExecutableMigration> migrations = new ArrayList<ExecutableMigration>();

        try {
            Class<?>[] classes = new ClassPathScanner().scanForClasses(basePackage, SpringJdbcMigration.class);
            for (Class<?> clazz : classes) {
                SpringJdbcMigration springJdbcMigration = (SpringJdbcMigration) ClassUtils.instantiate(clazz.getName());

                MigrationInfo migrationInfo = extractMigrationInfo(springJdbcMigration);
                String physicalLocation = ClassUtils.getLocationOnDisk(clazz);
                MigrationExecutor migrationExecutor = new SpringJdbcMigrationExecutor(springJdbcMigration);

                migrations.add(new ExecutableMigration(migrationInfo, physicalLocation, migrationExecutor));
            }
        } catch (Exception e) {
            throw new FlywayException("Unable to resolve Spring Jdbc Java migrations in location: " + basePackage, e);
        }

        return migrations;
    }

    /**
     * Extracts the migration info from this migration.
     *
     * @param springJdbcMigration The migration to analyse.
     * @return The migration info.
     */
    /* private -> testing */ MigrationInfo extractMigrationInfo(SpringJdbcMigration springJdbcMigration) {
        Integer checksum = null;
        if (springJdbcMigration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) springJdbcMigration;
            checksum = checksumProvider.getChecksum();
        }

        MigrationVersion version;
        String description;
        if (springJdbcMigration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider) springJdbcMigration;
            version = new MigrationVersion(infoProvider.getVersion().toString());
            description = infoProvider.getDescription();
        } else {
            String className = springJdbcMigration.getClass().getName();
            String classShortName = className.substring(className.lastIndexOf(".") + 1);
            String nameWithoutV = classShortName.substring(1);
            version = MigrationInfoHelper.extractVersion(nameWithoutV);
            description = MigrationInfoHelper.extractDescription(nameWithoutV);
        }

        String script = springJdbcMigration.getClass().getName();

        return new MigrationInfo(version, description, script, checksum, MigrationType.JAVA);
    }
}
