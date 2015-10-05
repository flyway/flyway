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

import org.flywaydb.core.internal.info.MigrationInfoData;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class NopValidationStrategyTest  {

    @Test
    public void testValidationFailedWithNullMigrationInfoData() throws Exception {
        NopValidationStrategy nopValidationStrategy = new NopValidationStrategy();

        assertFalse(nopValidationStrategy.validationFailed(null));
    }

    @Test
    public void testGetValidationErrorWithNullMigrationInfoData()  {
        NopValidationStrategy nopValidationStrategy = new NopValidationStrategy();

        assertNull(nopValidationStrategy.getValidationError(null));
    }

    @Test
    public void testGetValidationErrorAfterValidationFailedCallWithNullMigrationInfoData()  {
        NopValidationStrategy nopValidationStrategy = new NopValidationStrategy();

        nopValidationStrategy.validationFailed(null);
        assertNull(nopValidationStrategy.getValidationError(null));
    }

    @Test
    public void testValidationFailedWithMock() throws Exception {
        NopValidationStrategy nopValidationStrategy = new NopValidationStrategy();
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        assertFalse(nopValidationStrategy.validationFailed(migrationInfoDataMock));
    }

    @Test
    public void testGetValidationErrorWithMock()  {
        NopValidationStrategy nopValidationStrategy = new NopValidationStrategy();
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        assertNull(nopValidationStrategy.getValidationError(migrationInfoDataMock));
    }

    @Test
    public void testGetValidationErrorAfterValidationFailedCallWithMock()  {
        NopValidationStrategy nopValidationStrategy = new NopValidationStrategy();
        MigrationInfoData migrationInfoDataMock = mock(MigrationInfoData.class);

        nopValidationStrategy.validationFailed(migrationInfoDataMock);
        assertNull(nopValidationStrategy.getValidationError(migrationInfoDataMock));
    }

}
