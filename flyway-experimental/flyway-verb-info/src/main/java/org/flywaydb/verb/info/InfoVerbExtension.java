/*-
 * ========================LICENSE_START=================================
 * flyway-verb-info
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
package org.flywaydb.verb.info;

import java.sql.SQLException;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.verb.VerbUtils;

@CustomLog
public class InfoVerbExtension implements VerbExtension {
    @Override
    public boolean handlesVerb(final String verb) {
        return "info".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        LOG.debug("InfoVerbExtension.executeVerb");

        try {
            final var experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
            final var schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);

            final MigrationInfo[] migrations = VerbUtils.getMigrationInfos(configuration,
                experimentalDatabase,
                schemaHistoryModel);

            if (!experimentalDatabase.schemaHistoryTableExists(configuration.getTable())) {
                LOG.info("Schema history table " + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(),  configuration.getTable()) + " does not exist yet");
            }

            return new ExperimentalMigrationInfoService(migrations,
                configuration,
                experimentalDatabase.getName(),
                experimentalDatabase.allSchemasEmpty(VerbUtils.getAllSchemasFromConfiguration(configuration)));
        } catch (final SQLException e) {
            throw new FlywayException(e);
        }            
    }
}
