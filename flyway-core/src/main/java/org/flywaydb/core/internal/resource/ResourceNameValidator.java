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
package org.flywaydb.core.internal.resource;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@CustomLog
public class ResourceNameValidator {
    /**
     * Validates the names of all SQL resources returned by the ResourceProvider
     *
     * @param provider The ResourceProvider to validate
     * @param configuration The configuration to use
     */
    public void validateSQLMigrationNaming(ResourceProvider provider, Configuration configuration, DatabaseType databaseType) {

        List<String> errorsFound = new ArrayList<>();
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (Resource resource : getAllSqlResources(provider, configuration)) {
            String filename = resource.getFilename();
            LOG.debug("Validating " + filename);
            // Filter out special purpose files that the parser will not identify.
            if (isSpecialResourceFile(configuration, filename, databaseType)) {
                continue;
            }

            ResourceName result = resourceNameParser.parse(filename);
            if (!result.isValid()) {
                errorsFound.add(result.getValidityMessage());
            }
        }

        if (!errorsFound.isEmpty()) {
            if (configuration.isValidateMigrationNaming()) {
                throw new FlywayException("Invalid SQL filenames found:\r\n" + StringUtils.collectionToDelimitedString(errorsFound, "\r\n"));
            } else {
                LOG.info(errorsFound.size() + " SQL migrations were detected but not run because they did not follow the filename convention.");
                LOG.info("Set 'validateMigrationNaming' to true to fail fast and see a list of the invalid file names.");
            }
        }
    }

    private Collection<LoadableResource> getAllSqlResources(ResourceProvider provider, Configuration configuration) {
        return provider.getResources("", configuration.getSqlMigrationSuffixes());
    }

    private boolean isSpecialResourceFile(Configuration configuration, String filename, DatabaseType databaseType) {
        return databaseType != null && databaseType.getSpecialResourceFilenames(configuration)
            .contains(filename.toLowerCase());
    }
}
