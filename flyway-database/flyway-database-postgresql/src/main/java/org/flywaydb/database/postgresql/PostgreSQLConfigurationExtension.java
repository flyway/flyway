/*-
 * ========================LICENSE_START=================================
 * flyway-database-postgresql
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.database.postgresql;

import lombok.Data;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Data
public class PostgreSQLConfigurationExtension implements ConfigurationExtension {
    private static final String TRANSACTIONAL_LOCK = "flyway.postgresql.transactional.lock";

    private TransactionalModel transactional = null;

    public boolean isTransactionalLock() {
        // null is default, default is true, done this way for merge reasons.
        return transactional == null || transactional.getLock() == null || transactional.getLock();
    }

    public void setTransactionalLock(boolean transactionalLock) {
        transactional = new TransactionalModel();
        transactional.setLock(transactionalLock);
    }
    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK".equals(environmentVariable)) {
            return TRANSACTIONAL_LOCK;
        }
        return null;
    }

    @Override
    public String getNamespace() {
        return "postgresql";
    }
}
