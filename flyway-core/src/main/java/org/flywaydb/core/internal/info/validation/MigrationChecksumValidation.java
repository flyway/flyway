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

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.info.MigrationInfoData;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.util.ObjectUtils;

/**
 * Validation to detect checksum error between applied and resolved migration.
 */
public class MigrationChecksumValidation implements ValidationStrategy {

    public MigrationChecksumValidation() {
    }

    @Override
    public boolean validationFailed(MigrationInfoData migrationInfoData) {
        MigrationInfoContext context = migrationInfoData.getMigrationInfoContext();
        ResolvedMigration resolvedMigration = migrationInfoData.getResolvedMigration();
        AppliedMigration appliedMigration = migrationInfoData.getAppliedMigration();

        if (resolvedMigration != null && appliedMigration != null) {
            if (migrationInfoData.getVersion().compareTo(context.baseline) > 0) {
                if (!ObjectUtils.nullSafeEquals(resolvedMigration.getChecksum(), appliedMigration.getChecksum())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getValidationError(MigrationInfoData migrationInfoData) {
        if (validationFailed(migrationInfoData)) {
            ResolvedMigration resolvedMigration = migrationInfoData.getResolvedMigration();
            AppliedMigration appliedMigration = migrationInfoData.getAppliedMigration();

            return String.format("Migration Checksum mismatch for migration %s\n->Applied to database : %s\n-> Resolved locally    : %s",
                    appliedMigration.getScript(), appliedMigration.getChecksum(), resolvedMigration.getChecksum());
        }

        return null;
    }

}
