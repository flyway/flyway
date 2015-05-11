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

/**
 * This validation strategy will do nothing.
 * <p/>
 * {@link org.flywaydb.core.internal.info.validation.NopValidationStrategy#validationFailed(org.flywaydb.core.internal.info.MigrationInfoData)} will always return with
 * <code>false</code>.
 * <p/>
 * {@link org.flywaydb.core.internal.info.validation.NopValidationStrategy#getValidationError(org.flywaydb.core.internal.info.MigrationInfoData)} will always return with
 * <code>null</code>.
 */
public class NopValidationStrategy implements ValidationStrategy {

    public NopValidationStrategy() {
    }

    @Override
    public boolean validationFailed(MigrationInfoData migrationInfoData) {
        return false;
    }

    @Override
    public String getValidationError(MigrationInfoData migrationInfoData) {
        return null;
    }

 }
