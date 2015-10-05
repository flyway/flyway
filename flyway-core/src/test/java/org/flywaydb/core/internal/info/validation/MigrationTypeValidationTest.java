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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MigrationTypeValidationTest {

    @Test
    public void testValidationFailedNoError()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);

        boolean validationError = migrationTypeValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedNoAppliedMigration()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(null);

        boolean validationError = migrationTypeValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedNoResolvedMigration()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(null);

        boolean validationError = migrationTypeValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedVersionCheckTypeEqual()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;

        boolean validationError = migrationTypeValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedVersionCheckTypeDifferent()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;
        when(migrationInfoDataMock.getResolvedMigration().getType()).thenReturn(MigrationType.JDBC);

        boolean validationError = migrationTypeValidation.validationFailed(migrationInfoDataMock);

        assertTrue("validation error not detected", validationError);
    }

    @Test
    public void testValidationOnlyTypeDifferent()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        when(migrationInfoDataMock.getResolvedMigration().getType()).thenReturn(MigrationType.JDBC);

        boolean validationError = migrationTypeValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testGetValidationErrorNoError() {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);

        String validationErrorText = migrationTypeValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected",validationErrorText);
    }

    @Test
    public void testGetValidationErrorNoAppliedMigration() {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(null);

        String validationErrorText = migrationTypeValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected",validationErrorText);
    }

    @Test
    public void testGetValidationErrorFailedVersionCheckTypeEqual()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;

        String validationErrorText = migrationTypeValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected",validationErrorText);
    }

    @Test
    public void testGetValidationErrorFailedVersionCheckTypeDifferent()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;
        when(migrationInfoDataMock.getResolvedMigration().getType()).thenReturn(MigrationType.JDBC);

        String validationErrorText = migrationTypeValidation.getValidationError(migrationInfoDataMock);

        assertNotNull("Validation error not detected", validationErrorText);
        assertFalse("Error message must contain a string", validationErrorText.trim().isEmpty());
    }

    @Test
    public void testGetValidationErrorOnlyTypeDifferent()  {
        MigrationTypeValidation migrationTypeValidation = new MigrationTypeValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationType.SQL, MigrationVersion.LATEST);
        when(migrationInfoDataMock.getResolvedMigration().getType()).thenReturn(MigrationType.JDBC);

        String validationErrorText = migrationTypeValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected",validationErrorText);
    }

    private MigrationInfoData createMigrationInfoMockForValidation(MigrationType migrationTypeForFail, MigrationVersion migrationVersion) {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        AppliedMigration appliedMigrationMock = mock(AppliedMigration.class);
        ResolvedMigration resolvedMigrationMock = mock(ResolvedMigration.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.baseline = migrationVersion;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(resolvedMigrationMock);
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(appliedMigrationMock);

        when(appliedMigrationMock.getType()).thenReturn(migrationTypeForFail);
        when(resolvedMigrationMock.getType()).thenReturn(migrationTypeForFail);

        when(migrationInfoDataMock.getVersion()).thenReturn(migrationVersion);

        return migrationInfoDataMock;
    }

}
