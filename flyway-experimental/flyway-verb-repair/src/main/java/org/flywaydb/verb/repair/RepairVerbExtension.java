/*-
 * ========================LICENSE_START=================================
 * flyway-verb-repair
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
package org.flywaydb.verb.repair;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.RepairOutput;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.verb.VerbUtils;

public class RepairVerbExtension implements VerbExtension {
    @Override
    public boolean handlesVerb(final String verb) {
        return "repair".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        final ExperimentalDatabase experimentalDatabase;
        try {
            experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
        } catch (final Exception e) {
            throw new FlywayException(e);
        }

        final SchemaHistoryModel schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);

        final RepairResult repairResult = new RepairResult(VersionPrinter.getVersion(), experimentalDatabase.getDatabaseMetaData().databaseName());
        repairResult.migrationsRemoved = schemaHistoryModel.getSchemaHistoryItems().stream()
            .filter(x -> !x.isSuccess())
            .map(x -> new RepairOutput(x.getVersion() == null ? "" : x.getVersion(), x.getDescription(), "")).toList();
        if (!repairResult.migrationsRemoved.isEmpty()) {
            experimentalDatabase.removeFailedSchemaHistoryItems(configuration.getTable());
            repairResult.repairActions.add("Removed failed migrations");
        }

        return repairResult;
    }
}
