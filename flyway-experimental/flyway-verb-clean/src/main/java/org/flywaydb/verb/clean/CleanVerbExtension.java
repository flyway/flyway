/*-
 * ========================LICENSE_START=================================
 * flyway-verb-clean
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
package org.flywaydb.verb.clean;

import static org.flywaydb.core.experimental.ExperimentalModeUtils.logExperimentalDataTelemetry;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.verb.VerbUtils;

public class CleanVerbExtension implements VerbExtension {
    @Override
    public boolean handlesVerb(final String verb) {
        return "clean".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration, FlywayTelemetryManager flywayTelemetryManager) {
        if (configuration.isCleanDisabled()) {
            throw new FlywayException("Unable to execute clean as it has been disabled with the 'flyway.cleanDisabled' property.");
        }

        //TODO - get schemas created by flyway

        final ExperimentalDatabase experimentalDatabase;
        try {
            experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
        } catch (final Exception e) {
            throw new FlywayException(e);
        }

        logExperimentalDataTelemetry(flywayTelemetryManager, experimentalDatabase.getDatabaseMetaData());

        final List<String> schemas = new LinkedList<>(Arrays.asList(configuration.getSchemas()));
        final String defaultSchema = experimentalDatabase.getDefaultSchema(configuration);
        if (!schemas.contains(defaultSchema)) {
            schemas.add(0, defaultSchema);
        }

        final CleanResult cleanResult = new CleanResult(VersionPrinter.getVersion(), experimentalDatabase.getDatabaseMetaData().databaseName());
        cleanResult.operation = "clean";

        experimentalDatabase.doClean(schemas, cleanResult);

        return cleanResult;
    }
}
