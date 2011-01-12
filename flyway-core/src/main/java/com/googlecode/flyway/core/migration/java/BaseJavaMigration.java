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
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ClassUtils;

/**
 * Base class for java migration classes whose name conforms to the Flyway standard. Example: V1_2__Change_values
 *
 * @deprecated Implement JavaMigration directly instead. Also consider implementing JavaMigrationChecksumProvider and
 *             JavaMigrationInfoProvider if you need more control. This class will be removed in Flyway 1.5.
 */
@Deprecated
public abstract class BaseJavaMigration extends Migration implements JavaMigration, JavaMigrationInfoProvider, JavaMigrationChecksumProvider {
    /**
     * Initializes this Migration with this standard Flyway name.
     */
    protected BaseJavaMigration() {
        initVersionFromClassName();
        this.script = getClass().getName();
    }

    /**
     * Initializes this Migration with this version
     *
     * @param version     The version string for this migration, e.g. 1.2.3
     * @param description The description for this migration
     */
    protected BaseJavaMigration(String version, String description) {
        this.schemaVersion = new SchemaVersion(version);
        this.description = description;
        this.script = getClass().getName();
    }

    private void initVersionFromClassName() {
        String nameWithoutV = ClassUtils.getShortName(getClass()).substring(1);
        schemaVersion = MigrationInfoHelper.extractSchemaVersion(nameWithoutV);
        description = MigrationInfoHelper.extractDescription(nameWithoutV);
    }

    @Override
    public MigrationType getMigrationType() {
        return MigrationType.JAVA;
    }

    /**
     * Performs the migration.
     *
     * @param jdbcTemplate To execute the migration statements.
     * @param dbSupport    The support for database-specific extensions.
     *
     * @throws IllegalStateException Thrown when the migration failed.
     */
    @Override
    public final void migrate(final JdbcTemplate jdbcTemplate, final DbSupport dbSupport) throws IllegalStateException {
        try {
            doMigrateInTransaction(jdbcTemplate);
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        doMigrateInTransaction(jdbcTemplate);
    }

    /**
     * Performs the migration inside a transaction.
     *
     * @param jdbcTemplate To execute the migration statements.
     *
     * @throws org.springframework.dao.DataAccessException
     *          Thrown when the migration failed.
     */
    protected abstract void doMigrateInTransaction(JdbcTemplate jdbcTemplate) throws Exception;
}
