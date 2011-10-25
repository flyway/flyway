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
package com.googlecode.flyway.core.migration.init;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Special type of migration used to mark the initial state of the database from which Flyway can migrate to subsequent
 * versions. There can only be one init migration per database, and, if present, it must be the first one.
 */
public class InitMigration extends Migration {
    /**
     * Creates a new initial migration with this version.
     * <p/>
     * Only migrations with a version number higher than this one will be considered for this database.
     *
     * @param schemaVersion The initial version to put in the metadata table.
     */
    public InitMigration(SchemaVersion schemaVersion, String description) {
        if (schemaVersion == null) {
            this.schemaVersion = new SchemaVersion("0");
        } else {
            this.schemaVersion = schemaVersion;
        }

        if (description == null) {
            this.description = "<< Flyway Init >>";
        } else {
            this.description = description;
        }

        this.script = this.description;
    }

    @Override
    public String getLocation() {
        return script;
    }

    @Override
    public MigrationType getMigrationType() {
        return MigrationType.INIT;
    }

    @Override
    public Integer getChecksum() {
        return null;
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate, DbSupport dbSupport) throws DataAccessException {
        //Nothing to do
    }
}
