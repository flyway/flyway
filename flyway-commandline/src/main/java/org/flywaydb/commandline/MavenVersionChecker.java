/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.commandline;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.net.URL;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenVersionChecker {

    @Setter
    @Getter
    @NoArgsConstructor
    private static class MavenVersioning {
        private String release;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    private static class MavenMetadata {
        private MavenVersioning versioning;
    }

    private static final String FLYWAY_URL =

             "https://repo1.maven.org/maven2/org/flywaydb/flyway-core/maven-metadata.xml";





    public static void checkForVersionUpdates() {
        try {
            MigrationVersion current = MigrationVersion.fromVersion(VersionPrinter.getVersion());
            MavenMetadata metadata = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                    .readValue(new URL(FLYWAY_URL), MavenMetadata.class);

            MigrationVersion latest = MigrationVersion.fromVersion(metadata.getVersioning().getRelease());

            if (current.compareTo(latest) < 0) {
                LOG.warn("This version of Flyway is out of date. Upgrade to Flyway " + latest + ": "
                                 + FlywayDbWebsiteLinks.STAYING_UP_TO_DATE + "\n");
            }
        } catch (Exception e) {
            LOG.debug("Unable to check for updates: " + e.getMessage());
        }
    }
}