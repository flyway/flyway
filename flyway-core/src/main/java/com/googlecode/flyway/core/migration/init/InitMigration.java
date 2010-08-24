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

package com.googlecode.flyway.core.migration.init;

import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;

/**
 * Special type of migration used to mark the initial state of the database from which Flyway can migrate to subsequent
 * versions. There can only be one init migration per database, and, if present, it must be the first one.
 */
public class InitMigration extends Migration {
    /**
     * Creates a new initial migration with this version.
     * <p/>
     * Only migrations with a version number higher than this one
     * will be considered for this database.
     *
     * @param schemaVersion The initial version to put in the metadata table.
     */
    public InitMigration(SchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
        script = schemaVersion.getDescription();
    }

    @Override
    public MigrationType getMigrationType() {
        return MigrationType.INIT;
    }

    @Override
    public Integer getChecksum() {
        return null;
    }
}
