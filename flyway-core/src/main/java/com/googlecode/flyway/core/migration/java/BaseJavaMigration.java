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

package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.BaseMigration;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ClassUtils;

/**
 * Base class for java migration classes whose name conforms to the Flyway
 * standard. Example: V1_2__Change_values
 */
public abstract class BaseJavaMigration extends BaseMigration {
    /**
     * Initializes this Migration with this standard Flyway name.
     */
    protected BaseJavaMigration() {
        initVersionFromClassName();
        initScriptName();
    }

    /**
     * Initializes this Migration with this version
     *
     * @param version     The version string for this migration, e.g. 1.2.3
     * @param description The description for this migration
     */
    protected BaseJavaMigration(String version, String description) {
        this.schemaVersion = new SchemaVersion(version, description);
        initScriptName();
    }

    private void initScriptName() {
        this.script = getClass().getName();
        if (script.length() > 200) {
            script = script.substring(script.length() - 200);
        }
    }

    private void initVersionFromClassName() {
        String nameWithoutV = ClassUtils.getShortName(getClass()).substring(1);
        initVersion(nameWithoutV);
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
     * @throws IllegalStateException Thrown when the migration failed.
     */
    @Override
    public final void migrate(final JdbcTemplate jdbcTemplate, final DbSupport dbSupport) throws IllegalStateException {
        try {
            doMigrateInTransaction(jdbcTemplate);
        } catch (Exception e) {
            throw new IllegalStateException("Migration failed !", e);
        }
    }

    /**
     * Performs the migration inside a transaction.
     *
     * @param jdbcTemplate To execute the migration statements.
     * @throws org.springframework.dao.DataAccessException
     *          Thrown when the migration failed.
     */
    protected abstract void doMigrateInTransaction(JdbcTemplate jdbcTemplate) throws Exception;
}
