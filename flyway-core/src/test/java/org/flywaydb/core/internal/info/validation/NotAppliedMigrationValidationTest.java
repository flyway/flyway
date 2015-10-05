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
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.info.MigrationInfoData;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class NotAppliedMigrationValidationTest {

    @Test
    public void testValidationFailedWithPendingOrFuture() {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.pendingOrFuture = true;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getState()).thenReturn(MigrationState.FAILED);

        NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

        assertFalse("Validation should pass", notAppliedMigrationValidation.validationFailed(migrationInfoDataMock));
    }

    @Test
    public void testValidationFailedWithPendingOrFutureIgnored() {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.pendingOrFuture = true;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getState()).thenReturn(MigrationState.IGNORED);

        NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

        assertTrue("Validation should not pass", notAppliedMigrationValidation.validationFailed(migrationInfoDataMock));
    }

    @Test
    public void testValidationFailedWithNoPendingOrFutureIgnored() {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.pendingOrFuture = false;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getState()).thenReturn(MigrationState.IGNORED);

        NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

        assertTrue("Validation should not pass", notAppliedMigrationValidation.validationFailed(migrationInfoDataMock));
    }

    @Test
    public void testValidationFailedWithPendingOrFuturePending() {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.pendingOrFuture = true;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getState()).thenReturn(MigrationState.PENDING);

        NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

        assertFalse("Validation should pass", notAppliedMigrationValidation.validationFailed(migrationInfoDataMock));
    }

    @Test
    public void testValidationFailedWithNoPendingOrFuturePending() {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();
        migrationInfoContext.pendingOrFuture = false;

        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getState()).thenReturn(MigrationState.PENDING);

        NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

        assertTrue("Validation should not pass", notAppliedMigrationValidation.validationFailed(migrationInfoDataMock));
    }

    @Test
    public void testValidationFailedNoPendingMigrationAllState() {
        MigrationState[] migrationStates = MigrationState.values();
        ArrayList<MigrationState> migrationStateList = new ArrayList<MigrationState>(Arrays.asList(migrationStates));
        migrationStateList.remove(MigrationState.IGNORED);
        migrationStateList.remove(MigrationState.PENDING);

        ArrayList<String> listOfFailures = new ArrayList<String>();

        for (MigrationState migrationState : migrationStateList) {
            MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(migrationState);

            NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

            boolean validationError = notAppliedMigrationValidation.validationFailed(migrationInfoDataMock);

            if (validationError) {
                listOfFailures.add(String.format("Validation not passed for state %s. ", migrationState.name()));
            }
        }

        assertTrue("Wrong validation failure detected. see " + listOfFailures, listOfFailures.isEmpty());
    }

    @Test
    public void testGetValidationErrorIgnored() throws Exception {
        MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(MigrationState.IGNORED);

        NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

        String errorText = notAppliedMigrationValidation.getValidationError(migrationInfoDataMock);

        assertNotNull("Error text should not be null", errorText);
        assertFalse("Error text should contain text", errorText.trim().isEmpty());
        assertTrue("Error text should contain correct version", errorText.contains("1.7"));
    }

    @Test
    public void testGetValidationErrorAllState() {
        MigrationState[] migrationStates = MigrationState.values();
        ArrayList<MigrationState> migrationStateList = new ArrayList<MigrationState>(Arrays.asList(migrationStates));
        migrationStateList.remove(MigrationState.IGNORED);
        migrationStateList.remove(MigrationState.PENDING);

        ArrayList<String> listOfFailures = new ArrayList<String>();

        for (MigrationState migrationState : migrationStateList) {
            MigrationInfoData migrationInfoDataMock = createMigrationInfoMockForValidation(migrationState);

            NotAppliedMigrationValidation notAppliedMigrationValidation = new NotAppliedMigrationValidation();

            String validationError = notAppliedMigrationValidation.getValidationError(migrationInfoDataMock);

            if ( validationError != null ) {
                listOfFailures.add(String.format("Validation not passed for state %s with text '%s'", migrationState.name(), validationError));
            }
        }

        assertTrue("Wrong validation failure detected. see " + listOfFailures, listOfFailures.isEmpty());
    }

    private MigrationInfoData createMigrationInfoMockForValidation(MigrationState migrationState) {
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        AppliedMigration appliedMigrationMock = mock(AppliedMigration.class);
        MigrationInfoContext migrationInfoContext = new MigrationInfoContext();

        when(migrationInfoDataMock.getState()).thenReturn(migrationState);
        when(migrationInfoDataMock.getMigrationInfoContext()).thenReturn(migrationInfoContext);
        when(migrationInfoDataMock.getResolvedMigration()).thenReturn(null);
        when(migrationInfoDataMock.getAppliedMigration()).thenReturn(appliedMigrationMock);

        when(migrationInfoDataMock.getVersion()).thenReturn(MigrationVersion.fromVersion("1.7"));

        return migrationInfoDataMock;
    }
}
