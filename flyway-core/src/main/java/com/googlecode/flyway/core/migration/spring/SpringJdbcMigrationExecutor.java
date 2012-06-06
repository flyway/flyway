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

import com.googlecode.flyway.core.api.migration.MigrationChecksumProvider;
import com.googlecode.flyway.core.api.migration.MigrationInfoProvider;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationInfoHelper;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Adapter for executing migrations implementing SpringJdbcMigration.
 */
public class SpringJdbcMigrationExecutor extends Migration {
    /**
     * The SpringJdbcMigration to execute.
     */
    private final SpringJdbcMigration springJdbcMigration;

    /**
     * Creates a new SpringJdbcMigrationExecutor.
     *
     * @param springJdbcMigration The JavaMigration to execute.
     */
    public SpringJdbcMigrationExecutor(SpringJdbcMigration springJdbcMigration) {
        this.springJdbcMigration = springJdbcMigration;

        if (springJdbcMigration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) springJdbcMigration;
            checksum = checksumProvider.getChecksum();
        }

        if (springJdbcMigration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider) springJdbcMigration;
            schemaVersion = new SchemaVersion(infoProvider.getVersion().toString());
            description = infoProvider.getDescription();
        } else {
            String className = springJdbcMigration.getClass().getName();
            String classShortName = className.substring(className.lastIndexOf(".") + 1);
            String nameWithoutV = classShortName.substring(1);
            schemaVersion = MigrationInfoHelper.extractSchemaVersion(nameWithoutV);
            description = MigrationInfoHelper.extractDescription(nameWithoutV);
        }

        script = springJdbcMigration.getClass().getName();
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
            springJdbcMigration.migrate(new org.springframework.jdbc.core.JdbcTemplate(
                    new SingleConnectionDataSource(jdbcTemplate.getConnection(), true)));
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }
}
