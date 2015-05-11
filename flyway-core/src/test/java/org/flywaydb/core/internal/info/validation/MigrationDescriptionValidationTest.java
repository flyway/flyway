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
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.TestMethod;
import org.junit.rules.TestName;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MigrationDescriptionValidationTest {

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testValidationFailedNoError() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());

        boolean validationError = migrationDescriptionValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedNoAppliedMigration() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(null);

        boolean validationError = migrationDescriptionValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedNoResolvedMigration() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(null);

        boolean validationError = migrationDescriptionValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedVersionDescriptionEqual() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;

        boolean validationError = migrationDescriptionValidation.validationFailed(migrationInfoDataMock);

        assertFalse("validation not passed", validationError);
    }

    @Test
    public void testValidationFailedVersionDescriptionDifferent() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;
        when(migrationInfoDataMock.getAppliedMigration().getDescription()).thenReturn(testName.getMethodName()+testName.getMethodName());

        boolean validationError = migrationDescriptionValidation.validationFailed(migrationInfoDataMock);

        assertTrue("validation not passed", validationError);
    }


    @Test
    public void testGetValidationErrorNoError() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());

        String validationErrorText = migrationDescriptionValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected", validationErrorText);
    }

    @Test
    public void testGetValidationErrorMigration() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(null);

        String validationErrorText = migrationDescriptionValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected", validationErrorText);
    }

    @Test
    public void testGetValidationErrorNoResolvedMigration() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(null);

        String validationErrorText = migrationDescriptionValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected", validationErrorText);
    }

    @Test
    public void testGetValidationErrorFailedVersionDescriptionEqual() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;

        String validationErrorText = migrationDescriptionValidation.getValidationError(migrationInfoDataMock);

        assertNull("wrong validation error detected", validationErrorText);
    }

    @Test
    public void testGetValidationErrorFailedVersionDescriptionDifferent() {
        MigrationDescriptionValidation migrationDescriptionValidation = new MigrationDescriptionValidation();
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationVersion.LATEST, testName.getMethodName());
        migrationInfoDataMock.getMigrationInfoContext().baseline = MigrationVersion.CURRENT;
        when(migrationInfoDataMock.getAppliedMigration().getDescription()).thenReturn(testName.getMethodName()+testName.getMethodName());

        String validationErrorText = migrationDescriptionValidation.getValidationError(migrationInfoDataMock);

        assertNotNull("Validation error not detected", validationErrorText);
        assertFalse("Error message must contain a string", validationErrorText.trim().isEmpty());
    }


    private MigrationInfoData createMigrationInfoMockForValidation(MigrationVersion migrationVersion, String description) {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        AppliedMigration appliedMigrationMock = mock(AppliedMigration.class);
        ResolvedMigration resolvedMigrationMock = mock(ResolvedMigration.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.baseline = migrationVersion;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(resolvedMigrationMock);
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(appliedMigrationMock);

        when(appliedMigrationMock.getDescription()).thenReturn(description);
        when(resolvedMigrationMock.getDescription()).thenReturn(description);

        when(migrationInfoDataMock.getVersion()).thenReturn(migrationVersion);

        return migrationInfoDataMock;
    }
}
