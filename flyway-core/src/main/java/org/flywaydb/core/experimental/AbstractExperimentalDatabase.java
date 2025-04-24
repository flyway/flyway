/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.experimental;

import java.util.ArrayList;
import lombok.Getter;
import org.flywaydb.core.api.configuration.Configuration;

public abstract non-sealed class AbstractExperimentalDatabase <T> implements ExperimentalDatabase <T>{
    protected final ArrayList<String> batch = new ArrayList<>();
    protected MetaData metaData;

    @Getter
    protected ConnectionType connectionType;

    // This is the schema that contains the Schema History Table; it is not the schema of the current connection
    protected String currentSchema;

    @Override
    public void addToBatch(final String executionUnit) {
        batch.add(executionUnit);
    }

    @Override
    public int getBatchSize() {
        return batch.size();
    }

    @Override
    public final String getCurrentSchema() {
        return currentSchema;
    }

    protected abstract String getDefaultSchema(Configuration configuration);
}
