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

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.info.MigrationInfoData;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MissingClasspathValidationTest {

    private MigrationType[] validationExcludedMigrationTypes = new MigrationType[] {
         MigrationType.SCHEMA,
            MigrationType.BASELINE,
            MigrationType.INIT
    };

    @Test
    public void testValidationFailedWithPendingOrFuture()  {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.pendingOrFuture = true;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);

        MissingClasspathValidation missingClasspathValidation = new MissingClasspathValidation();

        assertFalse("Validation should pass",missingClasspathValidation.validationFailed(migrationInfoDataMock));
    }


    @Test
    public void testValidationFaileWithResolvedMigrationl()  {

        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        ResolvedMigration resolvedMigrationMock = mock(ResolvedMigration.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(resolvedMigrationMock);

        MissingClasspathValidation missingClasspathValidation = new MissingClasspathValidation();

        assertFalse("Validation should pass",missingClasspathValidation.validationFailed(migrationInfoDataMock));
    }


    @Test
    public void testValidationFailedWithAllTypes()  {
        ArrayList<String> listOfFailures = new ArrayList<String>();
        List<MigrationType> listOfMigrationTypeThatWouldNotFail = Arrays.asList(validationExcludedMigrationTypes);

        for (MigrationType migrationType : MigrationType.values()) {
            MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(migrationType);

            MissingClasspathValidation missingClasspathValidation = new MissingClasspathValidation();

            boolean validationFailed = missingClasspathValidation.validationFailed(migrationInfoDataMock);

            if (listOfMigrationTypeThatWouldNotFail.contains(migrationType)) {
                if ( validationFailed) {
                    listOfFailures.add(String.format("Validation should not fail for type %s.", migrationType.name()));
                }
            } else {
                if ( !validationFailed) {
                    listOfFailures.add(String.format("Validation should fail for type %s.", migrationType.name()));
                }
            }
        }

        assertTrue("Wrong validation failure detected. see " + listOfFailures, listOfFailures.isEmpty());
    }

    @Test
    public void testGetValidationErrorWithoutValidationFailedCall() {
        ArrayList<String> listOfFailures = new ArrayList<String>();
        List<MigrationType> listOfMigrationTypeThatWouldNotFail = Arrays.asList(validationExcludedMigrationTypes);

        for (MigrationType migrationType : MigrationType.values()) {
            MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(migrationType);

            MissingClasspathValidation missingClasspathValidation = new MissingClasspathValidation();

            String errorMessage = missingClasspathValidation.getValidationError(migrationInfoDataMock);

            if (listOfMigrationTypeThatWouldNotFail.contains(migrationType)) {
                // no error text
               if ( errorMessage != null ) {
                   listOfFailures.add(String.format("MigrationType '%s' produce wrong failure: '%s'",migrationType.name(), errorMessage));
               }
            } else {
                // must contain error text
                if ( errorMessage == null || errorMessage.trim().length() == 0 ) {
                    listOfFailures.add(String.format("MigrationType '%s' contains no error text: '%s'",migrationType.name(), errorMessage));
                }
            }
        }

        assertTrue("Wrong validation failure detected. see " + listOfFailures, listOfFailures.isEmpty());
    }


    private MigrationInfoData createMigrationInfoMockForValidation(MigrationType migrationTypeForFail) {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        AppliedMigration appliedMigrationMock = mock(AppliedMigration.class);
        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(null);
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(appliedMigrationMock);
        when(appliedMigrationMock.getType()).thenReturn(migrationTypeForFail);

        when(migrationInfoDataMock.getVersion()).thenReturn(MigrationVersion.fromVersion("1.5"));

        return migrationInfoDataMock;
    }

}
