/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.info.validation;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.info.MigrationInfoData;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;

/**
 * Validation if classpath contains not applied migrations.
 * <p/>
 * If {@link MigrationInfoContext#pendingOrFuture} have the value <code>false</code> than no validation will be done.
 */
public class NotAppliedMigrationValidation implements ValidationStrategy {

    public NotAppliedMigrationValidation() {
    }

    @Override
    public boolean validationFailed(MigrationInfoData migrationInfoData) {
        MigrationInfoContext context = migrationInfoData.getMigrationInfoContext();
        ResolvedMigration resolvedMigration = migrationInfoData.getResolvedMigration();
        AppliedMigration appliedMigration = migrationInfoData.getAppliedMigration();

        if ((!context.pendingOrFuture
                && (MigrationState.PENDING == migrationInfoData.getState()))
                || (MigrationState.IGNORED == migrationInfoData.getState())) {
            return true;
        }

        return false;
    }

    @Override
    public String getValidationError(MigrationInfoData migrationInfoData) {
        if (validationFailed(migrationInfoData)) {
            return String.format("Detected resolved migration not applied to database: %s", migrationInfoData.getVersion());
        }
        return null;
    }

}

