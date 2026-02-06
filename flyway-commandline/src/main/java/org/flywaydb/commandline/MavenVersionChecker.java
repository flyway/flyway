/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.commandline;

import static lombok.AccessLevel.PACKAGE;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.xml.XmlMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.CustomLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

@CustomLog
@RequiredArgsConstructor(access = PACKAGE)
class MavenVersionChecker {

    private final String flywayUrl;
    private final Supplier<MigrationVersion> currentVersionSupplier;

    MavenVersionChecker() {
        this(FLYWAY_URL, MavenVersionChecker::getCurrentVersion);
    }

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

    private static final int CONNECT_TIMEOUT_MS = 1000;
    private static final int REQUEST_TIMEOUT_MS = 8000;
    private static final String FLYWAY_URL =

             "https://repo1.maven.org/maven2/org/flywaydb/flyway-core/maven-metadata.xml";





    String checkForVersionUpdates() {
        try {
            final var metadata = getMavenMetadata();
            final var current = currentVersionSupplier.get();
            final var latest = MigrationVersion.fromVersion(metadata.getVersioning().getRelease());

            if (current.compareTo(latest) < 0) {
                return getMessage(latest);
            }
        } catch (final Exception e) {
            LOG.debug("Unable to check for updates: " + e.getMessage());
        }

        return null;
    }

    private MavenMetadata getMavenMetadata()
        throws URISyntaxException, ExecutionException, InterruptedException, JacksonException {
        final var url = new URI(flywayUrl);

        final var client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build();
        final var request = HttpRequest.newBuilder(url)
            .GET()
            .header("User-Agent", "Flyway")
            .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
            .build();
        final var response = client.sendAsync(request, BodyHandlers.ofString())
            .orTimeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .get();

        final var xmlMapper = XmlMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        return xmlMapper.readValue(response.body(), MavenMetadata.class);
    }

    private static MigrationVersion getCurrentVersion() {
        String currentVersion = VersionPrinter.getVersion();

        // Extra suffixes in the current version may cause MigrationVersion parsing to fail
        int idx = currentVersion.indexOf('-');
        currentVersion = idx >= 0 ? currentVersion.substring(0, idx) : currentVersion;

        return MigrationVersion.fromVersion(currentVersion);
    }

    private static String getMessage(final MigrationVersion latest) {
        final var message = "\nA more recent version of Flyway is available. Find out more about Flyway "
            + latest
            + " at "
            + FlywayDbWebsiteLinks.STAYING_UP_TO_DATE
            + "\n";
        final var border = "-".repeat(message.trim().length());
        return "\n" + border + message + border;
    }
}
