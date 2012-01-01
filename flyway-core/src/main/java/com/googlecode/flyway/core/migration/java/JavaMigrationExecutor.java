/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.ClassUtils;

/**
 * Adapter for executing migrations implementing JavaMigration.
 */
public class JavaMigrationExecutor extends Migration {
    /**
     * The JavaMigration to execute.
     */
    private final JavaMigration javaMigration;

    /**
     * Creates a new JavaMigrationExecutor.
     *
     * @param javaMigration The JavaMigration to execute.
     */
    public JavaMigrationExecutor(JavaMigration javaMigration) {
        this.javaMigration = javaMigration;

        if (ClassUtils.isAssignableValue(JavaMigrationChecksumProvider.class, javaMigration)) {
            JavaMigrationChecksumProvider checksumProvider = (JavaMigrationChecksumProvider) javaMigration;
            checksum = checksumProvider.getChecksum();
        }

        if (ClassUtils.isAssignableValue(JavaMigrationInfoProvider.class, javaMigration)) {
            JavaMigrationInfoProvider infoProvider = (JavaMigrationInfoProvider) javaMigration;
            schemaVersion = infoProvider.getVersion();
        } else {
            String nameWithoutV = ClassUtils.getShortName(javaMigration.getClass()).substring(1);
            schemaVersion = MigrationInfoHelper.extractSchemaVersion(nameWithoutV);
        }

        if (ClassUtils.isAssignableValue(JavaMigrationInfoProvider.class, javaMigration)) {
            JavaMigrationInfoProvider infoProvider = (JavaMigrationInfoProvider) javaMigration;
            description = infoProvider.getDescription();
        } else {
            String nameWithoutV = ClassUtils.getShortName(javaMigration.getClass()).substring(1);
            description = MigrationInfoHelper.extractDescription(nameWithoutV);
        }

        script = javaMigration.getClass().getName();
    }

    @Override
    public String getLocation() {
        return script;
    }

    @Override
    public MigrationType getMigrationType() {
        return MigrationType.JAVA;
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate, DbSupport dbSupport) {
        try {
            javaMigration.migrate(new org.springframework.jdbc.core.JdbcTemplate(
                    new SingleConnectionDataSource(jdbcTemplate.getConnection(), true)));
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }
}
