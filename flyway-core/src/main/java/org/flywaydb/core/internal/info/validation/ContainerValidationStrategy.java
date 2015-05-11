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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This validation strategy contains a list of validation strategies.<p/>
 *
 *  Order of invokation must be {@link #validationFailed(MigrationInfoData)},
 * {@link #getValidationError(MigrationInfoData)}.
 */
public class ContainerValidationStrategy implements ValidationStrategy {
    private ThreadLocal<String> validationErrorText = new ThreadLocal<String>();

    private List<ValidationStrategy> validationStrategies = new ArrayList<ValidationStrategy>();

    public ContainerValidationStrategy(ValidationStrategy...validationStrategiesArray) {
        validationStrategies.addAll(Arrays.asList(validationStrategiesArray));

        validationErrorText.set(null);
    }

    @Override
    public boolean validationFailed(MigrationInfoData migrationInfoData) {

        for ( ValidationStrategy validationStrategy : validationStrategies) {
            if ( validationStrategy.validationFailed(migrationInfoData)) {
                validationErrorText.set(validationStrategy.getValidationError(migrationInfoData));
                return true;
            }
        }

        return false;
    }

    @Override
    public String getValidationError(MigrationInfoData migrationInfoData) {
        return validationErrorText.get();
    }

}
