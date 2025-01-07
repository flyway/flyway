/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-scanners
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
package org.flywaydb.scanners;

import java.util.List;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@CustomLog
public class ScannerUtils {
    public static void validateMigrationNaming(final List<Pair<String, ResourceName>> resources,
        final boolean validateMigrationNaming, final String... sqlMigrationSuffixes) {
        final List<String> validErrors = resources.stream()
            .filter(pair -> !pair.getRight().isValid())
            .filter(pair -> StringUtils.startsAndEndsWith(pair.getLeft(), "", sqlMigrationSuffixes))
            .map(pair -> pair.getRight().getValidityMessage())
            .toList();
        if (!validErrors.isEmpty()) {
            if (validateMigrationNaming) {
                throw new FlywayException("Invalid SQL filenames found:\r\n" + StringUtils.collectionToDelimitedString(validErrors, "\r\n"));
            } else {
                LOG.info(validErrors.size() + " SQL migrations were detected but not run because they did not follow the filename convention.");
                LOG.info("Set 'validateMigrationNaming' to true to fail fast and see a list of the invalid file names.");
            }
        }
    }
}
