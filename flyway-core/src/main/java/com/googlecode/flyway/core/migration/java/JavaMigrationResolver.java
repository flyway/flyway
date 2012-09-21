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
package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.MigrationInfoImpl;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationExecutor;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.util.ClassUtils;
import com.googlecode.flyway.core.util.scanner.ClassPathScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration resolver for java class based migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 */
public class JavaMigrationResolver implements MigrationResolver {
    /**
     * The base package on the classpath where to migrations are located.
     */
    private final String basePackage;

    /**
     * Creates a new instance.
     *
     * @param basePackage The base package on the classpath where to migrations are located.
     */
    public JavaMigrationResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<ExecutableMigration> resolveMigrations() {
        List<ExecutableMigration> migrations = new ArrayList<ExecutableMigration>();

        try {
            Class<?>[] classes = new ClassPathScanner().scanForClasses(basePackage, JavaMigration.class);
            for (Class<?> clazz : classes) {
                JavaMigration javaMigration = (JavaMigration) ClassUtils.instantiate(clazz.getName());

                MigrationInfoImpl migrationInfo = extractMigrationInfo(javaMigration);
                String physicalLocation = ClassUtils.getLocationOnDisk(clazz);
                MigrationExecutor migrationExecutor = new JavaMigrationExecutor(javaMigration);

                migrations.add(new ExecutableMigration(migrationInfo, physicalLocation, migrationExecutor));
            }
        } catch (Exception e) {
            throw new FlywayException("Unable to resolve Java migrations in location: " + basePackage, e);
        }

        Collections.sort(migrations);
        return migrations;
    }

    /**
     * Extracts the migration info from this migration.
     *
     * @param javaMigration The migration to analyse.
     * @return The migration info.
     */
    /* private -> testing */ MigrationInfoImpl extractMigrationInfo(JavaMigration javaMigration) {
        Integer checksum = null;
        if (javaMigration instanceof JavaMigrationChecksumProvider) {
            JavaMigrationChecksumProvider checksumProvider = (JavaMigrationChecksumProvider) javaMigration;
            checksum = checksumProvider.getChecksum();
        }

        MigrationVersion version;
        String description;
        if (javaMigration instanceof JavaMigrationInfoProvider) {
            JavaMigrationInfoProvider infoProvider = (JavaMigrationInfoProvider) javaMigration;
            version = new MigrationVersion(infoProvider.getVersion().toString());
            description = infoProvider.getDescription();
        } else {
            String className = javaMigration.getClass().getName();
            String classShortName = className.substring(className.lastIndexOf(".") + 1);
            String nameWithoutV = classShortName.substring(1);
            version = MigrationInfoHelper.extractVersion(nameWithoutV);
            description = MigrationInfoHelper.extractDescription(nameWithoutV);
        }

        String script = javaMigration.getClass().getName();
        return new MigrationInfoImpl(version, description, script, checksum, MigrationType.JAVA);
    }
}
